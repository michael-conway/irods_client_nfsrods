package org.irods.nfsrods.vfs;

import java.io.IOException;
import java.util.Map;

import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.dcache.nfs.v4.NfsIdMapping;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.nfsrods.config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.ptr.IntByReference;

public class IRODSIdMap implements NfsIdMapping
{
    private static final Logger log_ = LoggerFactory.getLogger(IRODSIdMap.class);
    
    private static final LibC libc_ = (LibC) Native.load("c", LibC.class);

    public static final int NOBODY_UID = 65534;
    public static final int NOBODY_GID = 65534;

    public static final String NOBODY_USER  = libc_.getpwuid(NOBODY_UID).name;
    public static final String NOBODY_GROUP = libc_.getgrgid(NOBODY_GID).name;

    private final ServerConfig config_;
    private final IRODSAccessObjectFactory factory_;
    private Map<String, Integer> principalToUidMap_;
    private Map<Integer, IRODSUser> uidToPrincipalMap_;

    public IRODSIdMap(ServerConfig _config, IRODSAccessObjectFactory _factory)
    {
        config_ = _config;
        factory_ = _factory;
        principalToUidMap_ = new NonBlockingHashMap<>();
        uidToPrincipalMap_ = new NonBlockingHashMap<>();
        
        IRODSProxyAdminAccountConfig proxyConfig = _config.getIRODSProxyAdminAcctConfig();
        IRODSUser user = new IRODSUser(proxyConfig.getUsername(), 0, 0, config_, factory_);

        principalToUidMap_.put(proxyConfig.getUsername(), 0);
        uidToPrincipalMap_.put(0, user);
    }

    @Override
    public int principalToGid(String _principal)
    {
        log_.debug("principalToGid :: _principal = {}", _principal);
        
        return Integer.parseInt(_principal);
    }

    @Override
    public String gidToPrincipal(int _id)
    {
        log_.debug("gidToPrincipal :: _id = {}", _id);
        
        return String.valueOf(_id);
    }

    @Override
    public int principalToUid(String _principal)
    {
        log_.debug("principalToUid :: _principal = {}", _principal);
        
        return Integer.parseInt(_principal);
    }

    @Override
    public String uidToPrincipal(int _id)
    {
        log_.debug("uidToPrincipal :: _id = {}", _id);

        return String.valueOf(_id);
    }

    public int getUidForUser(String _name)
    {
        if (principalToUidMap_.containsKey(_name))
        {
            return principalToUidMap_.get(_name);
        }
        
        __password p = libc_.getpwnam(_name);
        
        if (p == null)
        {
            log_.debug("getUidForUser :: User not found. Returning uid {}", NOBODY_UID);
            return NOBODY_UID;
        }
        
        log_.debug("getUidForUser :: User found! Returning uid {}", p.uid);

        return p.uid;
    }
    
    public int getGidForUser(String _name)
    {
        if (principalToUidMap_.containsKey(_name))
        {
            IRODSUser user = uidToPrincipalMap_.get(principalToUidMap_.get(_name));
            return user.getGroupID();
        }
        
        __password p = libc_.getpwnam(_name);
        
        if (p == null)
        {
            log_.debug("getGidForUser :: User not found. Returning group name {}", NOBODY_GID);
            return NOBODY_GID;
        }
        
        log_.debug("getGidForUser :: User found! Returning group name {}", p.gid);

        return p.gid;
    }

    public IRODSUser resolveUser(int _uid) throws IOException
    {
        log_.debug("resolveUser - _userID = {}", _uid);

        IRODSUser user = uidToPrincipalMap_.get(_uid);
        
        if (user == null)
        {
            log_.debug("resolveUser - User not found in mapping. Looking up UID ...");

            __password p = libc_.getpwuid(_uid);
            
            if (p == null)
            {
                throw new IOException("User does not exist in the system.");
            }
            
            user = new IRODSUser(p.name, p.uid, p.gid, config_, factory_);

            principalToUidMap_.put(p.name, p.uid);
            uidToPrincipalMap_.put(p.uid, user);

            log_.debug("IRODSIdMap :: userName = {}", p.name);
        }
        
        return user;
    }
    
    @FieldOrder({"name", "passwd", "uid", "gid", "gecos", "dir", "shell"})
    public static class __password extends Structure
    {
        public String name;
        public String passwd;
        public int uid;
        public int gid;
        public String gecos;
        public String dir;
        public String shell;
    }

    @FieldOrder({"name", "passwd", "gid", "mem"})
    public static class __group extends Structure
    {
        public String name;
        public String passwd;
        public int gid;
        public Pointer mem;
    }
    
    private static interface LibC extends Library
    {
        __password getpwnam(String name);
        __password getpwuid(int id);
        __group getgrnam(String name);
        __group getgrgid(int id);
        int getgrouplist(String user, int gid, int[] groups, IntByReference ngroups);
    }
}
