/**
 * 
 */
package org.irods.nfsrods.vfs.testmocks;

/**
 * Translate test users into a mapping of gid and uid
 * 
 * @author Mike Conway - NIEHS
 *
 */
public class MockMappingEntry {

	private String principal = "";
	private int gid = 0;
	private int uid = 0;

	/**
	 * 
	 */
	public MockMappingEntry() {
	}

	public String getPrincipal() {
		return principal;
	}

	public void setPrincipal(String principal) {
		this.principal = principal;
	}

	public int getGid() {
		return gid;
	}

	public void setGid(int gid) {
		this.gid = gid;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MockMappingEntry [principal=").append(principal).append(", gid=").append(gid).append(", uid=")
				.append(uid).append("]");
		return builder.toString();
	}

}
