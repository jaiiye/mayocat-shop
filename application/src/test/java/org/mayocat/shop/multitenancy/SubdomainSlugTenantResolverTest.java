package org.mayocat.shop.multitenancy;

import static org.junit.Assert.*;

import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.mayocat.shop.configuration.MultitenancyConfiguration;
import org.mayocat.shop.model.Tenant;
import org.mayocat.shop.service.AccountsService;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.test.jmock.AbstractMockingComponentTestCase;
import org.xwiki.test.jmock.annotation.MockingRequirement;

@MockingRequirement(value = SubdomainSlugTenantResolver.class)
public class SubdomainSlugTenantResolverTest extends AbstractMockingComponentTestCase<TenantResolver>
{
    private TenantResolver tenantResolver;

    private MultitenancyConfiguration configuration;

    private AccountsService accountsService;

    /**
     * Setup mock dependencies before initializing the @MockingRequirement components.
     */
    public void setupDependencies() throws Exception
    {
        // Allow to mock classes for mocking configuration instances
        getMockery().setImposteriser(ClassImposteriser.INSTANCE);

        configuration = getMockery().mock(MultitenancyConfiguration.class);
 
        DefaultComponentDescriptor<MultitenancyConfiguration> cd =
            new DefaultComponentDescriptor<MultitenancyConfiguration>();
        cd.setRoleType(MultitenancyConfiguration.class);
        this.getComponentManager().registerComponent(cd, this.configuration);
    }

    @Before
    @Override
    public void setUp() throws Exception
    {
        getMockery().setImposteriser(ClassImposteriser.INSTANCE);
        //this.setupDependencies();

        super.setUp();

        configuration = this.getComponentManager().getInstance(MultitenancyConfiguration.class);
        accountsService = this.getComponentManager().getInstance(AccountsService.class);
        tenantResolver = getMockedComponent();

        getMockery().checking(new Expectations()
        {
            {
                allowing(configuration).getDefaultTenantSlug();
                will(returnValue("mytenant"));

                allowing(configuration).getRootDomain();
                will(returnValue(null));

                allowing(accountsService).findTenant(with(Matchers.not(equal("mytenant"))));
                will(returnValue(null));

                allowing(accountsService).findTenant(with(equal("mytenant")));
                will(returnValue(new Tenant("mytenant", null)));

                allowing(accountsService).createTenant(with(any(Tenant.class)));

            }
        });
    }

    @Test
    public void testMultitenancyNotActivatedReturnsDefaultTenant1() throws Exception
    {
        this.setUpExpectationsForMultitenancyNotActivated();
        assertNotNull(this.tenantResolver.resolve("mayocatshop.com"));
        assertEquals("mytenant", this.tenantResolver.resolve("mayocatshop.com").getSlug());
    }

    @Test
    public void testMultitenancyNotActivatedReturnsDefaultTenant2() throws Exception
    {
        this.setUpExpectationsForMultitenancyNotActivated();
        assertNotNull(this.tenantResolver.resolve("localhost"));
        assertEquals("mytenant", this.tenantResolver.resolve("localhost").getSlug());
    }

    @Test
    public void testMultitenancyTenantResolver1() throws Exception
    {
        this.setUpExpectationsForMultitenancyActivated();

        assertNotNull(this.tenantResolver.resolve("mytenant.mayocatshop.com"));
        assertEquals("mytenant", this.tenantResolver.resolve("mytenant.mayocatshop.com").getSlug());
    }

    @Test
    public void testMultitenancyTenantResolver2() throws Exception
    {
        this.setUpExpectationsForMultitenancyActivated();

        assertNotNull(this.tenantResolver.resolve("mytenant.localhost"));
        assertEquals("mytenant", this.tenantResolver.resolve("mytenant.localhost").getSlug());
    }

    @Test
    public void testMultitenancyTenantResolver3() throws Exception
    {
        this.setUpExpectationsForMultitenancyActivated();

        assertNull(this.tenantResolver.resolve("localhost"));
    }

    @Test
    public void testMultitenancyTenantResolver4() throws Exception
    {
        this.setUpExpectationsForMultitenancyActivated();

        assertNull(this.tenantResolver.resolve("mayocatshop.com"));
    }

    @Test
    public void testMultitenancyTenantResolver5() throws Exception
    {
        //TODO
        // Test IP addresses resolving.
        // (right now it throws a illegal argument exception
    }


    // ///////////////////////////////////////////////////////////////////////////////////

    private void setUpExpectationsForMultitenancyActivated()
    {
        getMockery().checking(new Expectations()
        {
            {
                allowing(configuration).isActivated();
                will(returnValue(true));

                allowing(configuration).getRootDomain();
                will(returnValue(null));
            }
        });
    }

    private void setUpExpectationsForMultitenancyNotActivated()
    {
        getMockery().checking(new Expectations()
        {
            {
                allowing(configuration).isActivated();
                will(returnValue(false));

                allowing(configuration).getDefaultTenantSlug();
                will(returnValue("mytenant"));
            }
        });
    }
}