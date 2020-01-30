package org.irods.nfsrods.vfs;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import org.dcache.nfs.vfs.Inode;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.nfsrods.config.ConnectionManagementConfig;
import org.irods.nfsrods.config.IRODSClientConfig;
import org.irods.nfsrods.config.IRODSProxyAdminAccountConfig;
import org.irods.nfsrods.config.NFSServerConfig;
import org.irods.nfsrods.config.ServerConfig;
import org.irods.nfsrods.vfs.testmocks.MockMappingEntry;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class IRODSVirtualFileSystemTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IRODSVirtualFileSystemTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(testingProperties);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testBaseConstructorWithMockIdMapperGetRoodInode() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAccount irodsAccountSecondary = testingPropertiesHelper
				.buildIRODSAccountFromSecondaryTestProperties(testingProperties);
		IRODSAccount proxyAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		JargonProperties jargonProperties = irodsFileSystem.getJargonProperties();
		IRODSProxyAdminAccountConfig irodsProxyAdminAccountConfig = new IRODSProxyAdminAccountConfig(
				proxyAccount.getUserName(), proxyAccount.getPassword());
		NFSServerConfig nfsServerConfig = new NFSServerConfig(new Integer(2049),
				MiscIRODSUtils.buildIRODSUserHomeForAccountUsingDefaultScheme(irodsAccount), new Integer(3600000),
				new Integer(1000), new Integer(1000));
		ConnectionManagementConfig connectionManagementConfig = new ConnectionManagementConfig(
				ConnectionManagementConfig.MODE_CACHE, 0, 0, 0, false, false, false);
		IRODSClientConfig irodsClientConfig = new IRODSClientConfig(irodsAccount.getHost(), irodsAccount.getPort(),
				irodsAccount.getZone(), irodsAccount.getDefaultStorageResource(),
				irodsFileSystem.getJargonProperties().getNegotiationPolicy().toString(),
				jargonProperties.getIRODSSocketTimeout(), connectionManagementConfig, irodsProxyAdminAccountConfig);

		ServerConfig serverConfig = new ServerConfig(nfsServerConfig, irodsClientConfig);
		IRODSFileSystem ifs = ServerMain.standupCachedConnectionMode(serverConfig.getNfsServerConfig());
		List<MockMappingEntry> entries = new ArrayList<MockMappingEntry>();
		MockMappingEntry entry = new MockMappingEntry();
		entry.setGid(1000);
		entry.setUid(1000);
		entry.setPrincipal(irodsAccount.getUserName());
		entries.add(entry);

		entry = new MockMappingEntry();
		entry.setGid(1001);
		entry.setUid(1001);
		entry.setPrincipal(irodsAccountSecondary.getUserName());
		entries.add(entry);
		IRODSIdMapper mapping = Mockito.mock(IRODSIdMapper.class);

		CachingProvider cachingProvider = Caching.getCachingProvider();
		CacheManager cacheManager = cachingProvider.getCacheManager();

		IRODSVirtualFileSystem ivfs = new IRODSVirtualFileSystem(serverConfig, ifs.getIRODSAccessObjectFactory(),
				mapping, cacheManager);
		Inode actual = ivfs.getRootInode();
		Assert.assertNotNull("no root inode", actual);
		ifs.closeAndEatExceptions();
		Assert.assertTrue(true);

	}
}
