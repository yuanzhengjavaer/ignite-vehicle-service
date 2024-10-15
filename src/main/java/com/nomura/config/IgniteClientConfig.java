package com.nomura.config;

import com.nomura.common.Constants;
import com.nomura.components.filter.VehicleServiceFilter;
import com.nomura.service.VehicleService;
import com.nomura.service.impl.VehicleServiceImpl;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.TransactionConfiguration;
import org.apache.ignite.services.ServiceConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.springframework.boot.autoconfigure.IgniteConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.ignite.logger.log4j2.Log4J2Logger;

import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class IgniteClientConfig {

    @Bean
    public IgniteConfigurer clientConfiguration() {
        return new IgniteConfigurer() {
            @Override
            public void accept(IgniteConfiguration igniteConfiguration) {
                // If you provide a whole ClientConfiguration bean then configuration properties will not be used.
                igniteConfiguration.setPeerClassLoadingEnabled(true);
//                igniteConfiguration.setClientMode(true);
                igniteConfiguration.setServiceConfiguration(serviceConfiguration());
                igniteConfiguration.setUserAttributes(getUserAttributes());
                igniteConfiguration.setDiscoverySpi(tcpDiscoverySpi());

                //transactionnに関する
//                TransactionConfiguration defaultTxConfig = new TransactionConfiguration();
//                defaultTxConfig.setDefaultTxTimeout(5000L);
//                defaultTxConfig.setTxSerializableEnabled(true);
//                igniteConfiguration.setTransactionConfiguration(defaultTxConfig);
//                CacheConfiguration cacheConfiguration = new CacheConfiguration("vehicles");
//                cacheConfiguration.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
//                igniteConfiguration.setCacheConfiguration(cacheConfiguration);
                try {
                    String logConfigPath = this.getClass().
                            getClassLoader().
                            getResource("ignite-log4j2.xml").getPath();
                    logConfigPath = URLDecoder.decode(logConfigPath, "utf-8");
                    igniteConfiguration.setGridLogger(new Log4J2Logger(logConfigPath));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private ServiceConfiguration serviceConfiguration() {
        ServiceConfiguration svcConfig = new ServiceConfiguration();
        svcConfig.setService(vehicleService());
        svcConfig.setName(VehicleService.SERVICE_NAME);
        svcConfig.setMaxPerNodeCount(1);
        svcConfig.setNodeFilter(new VehicleServiceFilter());
        return svcConfig;
    }

//    @Bean
    public VehicleService vehicleService() {
        return new VehicleServiceImpl();
    }

    @Bean
    public TcpDiscoveryVmIpFinder tcpDiscoveryVmIpFinder() {
        TcpDiscoveryVmIpFinder tcpDiscoveryVmIpFinder = new TcpDiscoveryVmIpFinder();
        tcpDiscoveryVmIpFinder.setAddresses(Arrays.asList("127.0.0.1:47500..47509"));
        return tcpDiscoveryVmIpFinder;
    }

    public TcpDiscoverySpi tcpDiscoverySpi() {
        TcpDiscoverySpi tcpDiscoverySpi = new TcpDiscoverySpi();
        tcpDiscoverySpi.setIpFinder(tcpDiscoveryVmIpFinder());
        return tcpDiscoverySpi;
    }

    private Map<String, ?> getUserAttributes() {
        Map<String, Object> userAttributes = new HashMap<>();
        userAttributes.put(Constants.SERVICE_NODE_TAG, true);
        return userAttributes;
    }

}
