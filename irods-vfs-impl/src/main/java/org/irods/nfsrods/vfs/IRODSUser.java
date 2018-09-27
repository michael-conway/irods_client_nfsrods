package org.irods.nfsrods.vfs;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.nfsrods.config.IRODSProxyAdminAccountConfig;
import org.irods.nfsrods.config.IRODSServerConfig;
import org.irods.nfsrods.config.NFSServerConfig;
import org.irods.nfsrods.config.ServerConfig;
import org.irods.nfsrods.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

public class IRODSUser
{
    private static final Logger log_ = LoggerFactory.getLogger(IRODSIdMap.class);

    private NonBlockingHashMap<Long, Path> inodeToPath_;
    private NonBlockingHashMap<Path, Long> pathToInode_;
    private List<Long> availableInodeNumbers_;
    private AtomicLong fileID_;
    private IRODSAccessObjectFactory factory_;
    private IRODSAccount proxiedAcct_;
    private IRODSFile rootFile_;
    private int userID_;

    public IRODSUser(String _username, ServerConfig _config, IRODSAccessObjectFactory _factory)
    {
        inodeToPath_ = new NonBlockingHashMap<>();
        pathToInode_ = new NonBlockingHashMap<>();
        availableInodeNumbers_ = new ArrayList<>();
        fileID_ = new AtomicLong(1); // Inode numbers start at 1

        NFSServerConfig nfsSvrConfig = _config.getNfsServerConfig();
        IRODSProxyAdminAccountConfig proxyConfig = _config.getIRODSProxyAdminAcctConfig();
        IRODSServerConfig rodsSvrConfig = _config.getIRODSServerConfig();

        String adminAcct = proxyConfig.getUsername();
        String adminPw = proxyConfig.getPassword();
        String zone = rodsSvrConfig.getZone();

        String rootPath = Paths.get(nfsSvrConfig.getIRODSMountPoint()).toString();
        log_.debug("IRODSUser :: iRODS mount point = {}", rootPath);
        log_.debug("IRODSUser :: Creating proxy for username [{}] ...", _username);

        try
        {
            proxiedAcct_ = IRODSAccount.instanceWithProxy(rodsSvrConfig.getHost(), rodsSvrConfig.getPort(), _username,
                                                          adminPw, rootPath, zone, rodsSvrConfig.getDefaultResource(),
                                                          adminAcct, zone);
            factory_ = _factory;
            rootFile_ = factory_.getIRODSFileFactory(proxiedAcct_).instanceIRODSFile(rootPath);

            User user = factory_.getUserAO(proxiedAcct_).findByName(_username);
            userID_ = Integer.parseInt(user.getId());

            establishRoot();
        }
        catch (JargonException e)
        {
            log_.error(e.getMessage());
        }
    }

    public int getUserID()
    {
        return this.userID_;
    }

    public String getAbsolutePath()
    {
        return rootFile_.getAbsolutePath();
    }

    public NonBlockingHashMap<Long, Path> getInodeToPathMap()
    {
        try
        {
            log_.debug("getInodeToPathMap :: data = > {}", JSONUtils.toJSON(inodeToPath_));
        }
        catch (JsonProcessingException e)
        {
            log_.error(e.getMessage());
        }

        return inodeToPath_;
    }

    public NonBlockingHashMap<Path, Long> getPathToInodeMap()
    {
        try
        {
            log_.debug("getPathToInodeMap :: data = > {}", JSONUtils.toJSON(pathToInode_));
        }
        catch (JsonProcessingException e)
        {
            log_.error(e.getMessage());
        }

        return pathToInode_;
    }

    public Long getAndIncrementFileID()
    {
        if (!availableInodeNumbers_.isEmpty())
        {
            return availableInodeNumbers_.remove(0);
        }

        return fileID_.getAndIncrement();
    }

    public IRODSAccessObjectFactory getIRODSAccessObjectFactory()
    {
        return factory_;
    }

    public IRODSAccount getAccount()
    {
        return proxiedAcct_;
    }

    public IRODSFile getRoot()
    {
        return rootFile_;
    }

    private void establishRoot()
    {
        if (!rootFile_.exists())
        {
            log_.error("Root file does not exist or it cannot be read");

            try
            {
                throw new DataNotFoundException("Cannot establish root at [" + rootFile_ + "].");
            }
            catch (DataNotFoundException e)
            {
                log_.error(e.getMessage());
            }
            
            return;
        }

        log_.debug("establishRoot :: Mapping root to [{}] ...", rootFile_);

        map(getAndIncrementFileID(), rootFile_.getAbsolutePath());

        log_.debug("establishRoot :: Mapping successful.");
    }

    public void map(Long _inodeNumber, String _path)
    {
        map(_inodeNumber, Paths.get(_path));
    }

    public void map(Long _inodeNumber, Path _path)
    {
        if (inodeToPath_.putIfAbsent(_inodeNumber, _path) != null)
        {
            // FIXME Add message.
            throw new IllegalStateException();
        }

        Long otherInodeNumber = pathToInode_.putIfAbsent(_path, _inodeNumber);

        if (otherInodeNumber != null)
        {
            // try rollback
            if (inodeToPath_.remove(_inodeNumber) != _path)
            {
                // FIXME Add message.
                throw new IllegalStateException("Cannot remove mapping, rollback failed.");
            }

            // FIXME Add message.
            throw new IllegalStateException("path ");
        }
    }

    public void unmap(Long _inodeNumber, Path _path)
    {
        Path removedPath = inodeToPath_.remove(_inodeNumber);

        if (!_path.equals(removedPath))
        {
            // FIXME Add message.
            throw new IllegalStateException();
        }

        if (pathToInode_.remove(_path) != _inodeNumber)
        {
            // FIXME Add message.
            throw new IllegalStateException();
        }
        
        availableInodeNumbers_.add(_inodeNumber);
    }

    public void remap(Long _inodeNumber, Path _oldPath, Path _newPath)
    {
        unmap(_inodeNumber, _oldPath);
        map(_inodeNumber, _newPath);
    }

    @Override
    public String toString()
    {
        return "IRODSUser{rootAccount=" + proxiedAcct_ + ", userID=" + userID_ + '}';
    }

}
