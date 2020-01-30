/**
 * 
 */
package org.irods.nfsrods.vfs.testmocks;

import java.io.IOException;
import java.util.List;

import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.nfsrods.config.ServerConfig;
import org.irods.nfsrods.vfs.IRODSIdMapper;

/**
 * Mock of the NFS Id mapper
 * 
 * @author Mike Conway - NIEHS
 *
 */
public class MockNfsIdMapping extends IRODSIdMapper {

	private final List<MockMappingEntry> mapEntries;

	/**
	 * 
	 */
	public MockNfsIdMapping(List<MockMappingEntry> mapEntries, ServerConfig config, IRODSAccessObjectFactory factory)
			throws IOException {
		super(config, factory);
		this.mapEntries = mapEntries;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dcache.nfs.v4.NfsIdMapping#gidToPrincipal(int)
	 */
	@Override
	public String gidToPrincipal(int gid) {

		for (MockMappingEntry entry : mapEntries) {
			if (entry.getGid() == gid) {
				return entry.getPrincipal();
			}
		}

		throw new IllegalArgumentException("invalid gid");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dcache.nfs.v4.NfsIdMapping#principalToGid(java.lang.String)
	 */
	@Override
	public int principalToGid(String principal) {
		for (MockMappingEntry entry : mapEntries) {
			if (entry.getPrincipal().equals(principal)) {
				return entry.getGid();
			}
		}

		throw new IllegalArgumentException("invalid principal");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dcache.nfs.v4.NfsIdMapping#principalToUid(java.lang.String)
	 */
	@Override
	public int principalToUid(String principal) {
		for (MockMappingEntry entry : mapEntries) {
			if (entry.getPrincipal().equals(principal)) {
				return entry.getUid();
			}
		}

		throw new IllegalArgumentException("invalid principal");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dcache.nfs.v4.NfsIdMapping#uidToPrincipal(int)
	 */
	@Override
	public String uidToPrincipal(int uid) {
		for (MockMappingEntry entry : mapEntries) {
			if (entry.getUid() == uid) {
				return entry.getPrincipal();
			}
		}

		throw new IllegalArgumentException("invalid uid");
	}

}
