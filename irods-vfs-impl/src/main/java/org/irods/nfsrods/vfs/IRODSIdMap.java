package org.irods.nfsrods.vfs;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.dcache.auth.Subjects;
import org.dcache.nfs.v4.NfsIdMapping;
import org.dcache.nfs.v4.NfsLoginService;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.nfsrods.config.IdMapConfigEntry;
import org.irods.nfsrods.config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IRODSIdMap implements NfsIdMapping, NfsLoginService//, RpcLoginService
{
    private static final Logger log_ = LoggerFactory.getLogger(IRODSIdMap.class);

    private static final int NOBODY_UID = 65534;
    private static final int NOBODY_GID = 65534;

    private final ServerConfig config_;
    private final List<IdMapConfigEntry> idMapConfig_;
    private final IRODSAccessObjectFactory factory_;
    private Map<String, Integer> principalToUidMap_;
    private Map<Integer, IRODSUser> uidToPrincipalMap_;

    public IRODSIdMap(ServerConfig _config, List<IdMapConfigEntry> _idMapConfig, IRODSAccessObjectFactory _factory)
    {
        config_ = _config;
        idMapConfig_ = _idMapConfig;
        factory_ = _factory;
        principalToUidMap_ = new NonBlockingHashMap<>();
        uidToPrincipalMap_ = new NonBlockingHashMap<>();
        
        initIdMappings();
    }
    
    private void initIdMappings()
    {
        idMapConfig_.forEach(e -> {
            IRODSUser user = new IRODSUser(e.getName(), config_, factory_);
            
            principalToUidMap_.put(e.getName(), e.getUserId() /* original: user.getUserID() */);
            uidToPrincipalMap_.put(e.getUserId() /* original: user.getUserID() */, user);

            log_.debug("IRODSIdMap :: userName = {}", e.getName());
        });
    }

    @Override
    public int principalToGid(String _principal)
    {
        try
        {
            log_.debug("principalToGid :: _principal = {}", Integer.parseInt(_principal));
            return Integer.parseInt(_principal);
        }
        catch (NumberFormatException e)
        {
            log_.error(e.getMessage());
        }

        return NOBODY_GID;
    }

    @Override
    public int principalToUid(String _principal)
    {
        try
        {
            log_.debug("principalToUid :: _principal = {}", Integer.parseInt(_principal));
            return Integer.parseInt(_principal);
        }
        catch (NumberFormatException e)
        {
            log_.error(e.getMessage());
        }

        return NOBODY_UID;
    }

    @Override
    public String uidToPrincipal(int _id)
    {
        log_.debug("uidToPrincipal :: _id = {}", Integer.toString(_id));
        return Integer.toString(_id);
    }

    @Override
    public String gidToPrincipal(int _id)
    {
        log_.debug("gidToPrincipal :: _id = {}", Integer.toString(_id));
        return Integer.toString(_id);
    }

    @Override
    public Subject login(Principal _principal)
    {
        int uid = principalToUidMap_.get(_principal.getName());
        return Subjects.of(uid, uid);
    }

//    @Override
//    public Subject login(RpcTransport _rpcTransport, GSSContext _gssCtx)
//    {
//        try
//        {
//            String principal = _gssCtx.getSrcName().toString();
//            Integer rodsUserID = principleUidMap_.get(principal);
//
//            // printPrincipalType(principal);
//
//            if (rodsUserID == null)
//            {
//                String userName = null;
//
//                // If the principal represents a service.
//                if (principal != null && principal.startsWith("nfs/"))
//                {
//                    userName = config_.getIRODSProxyAdminAcctConfig().getUsername();
//                }
//                else
//                {
//                    // KerberosPrincipal kp = new KerberosPrincipal(principal);
//                    // userName = kp.getName();
//                    userName = principal.substring(0, principal.indexOf('@'));
//                    log_.debug("IRODSIdMap :: userName = {}", userName);
//                }
//
//                IRODSUser user = new IRODSUser(userName, config_, factory_);
//                rodsUserID = user.getUserID();
//                principleUidMap_.put(principal, rodsUserID);
//                irodsPrincipleMap_.put(rodsUserID, user);
//            }
//
//            return Subjects.of(rodsUserID, rodsUserID);
//        }
//        catch (GSSException e)
//        {
//            log_.error(e.getMessage());
//        }
//
//        return Subjects.of(NOBODY_UID, NOBODY_GID);
//    }

    public IRODSUser resolveUser(int _userID)
    {
        log_.debug("resolveUser - _userID = {}", _userID);
        return uidToPrincipalMap_.get(Integer.valueOf(_userID));
    }
}
