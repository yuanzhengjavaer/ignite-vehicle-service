/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.nomura.service.impl;

import com.nomura.model.po.Vehicle;
import com.nomura.service.VehicleService;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteAtomicSequence;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.services.ServiceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;


/**
 * An implementation of {@link VehicleService} that will be deployed in the cluster.
 * </p>
 * The implementation stores vehicle's data in a dedicated distributed cache deployed on Data Nodes.
 */
public class VehicleServiceImpl implements VehicleService {
    @IgniteInstanceResource
    @Autowired
    @Lazy
    private Ignite ignite;

    /**
     * Reference to the cache.
     */
    private IgniteCache<Integer, Vehicle> vehiclesCache;

    /**
     * Maintenance IDs generator
     */
    private IgniteAtomicSequence sequence;

    /**
     * {@inheritDoc}
     */
    public void init(ServiceContext ctx) throws Exception {
        System.out.println("Initializing Vehicle Service on node:" + ignite.cluster().localNode());

        /**
         * It's assumed that the cache has already been deployed. To do that, make sure to start Data Nodes with
         * a respective cache configuration.
         */
        vehiclesCache = ignite.cache("vehicles");
    }

    /**
     * {@inheritDoc}
     */
    public void execute(ServiceContext ctx) throws Exception {
        System.out.println("Executing Vehicle Service on node:" + ignite.cluster().localNode());

        // Some custom logic.
        sequence = ignite.atomicSequence("MaintenanceIds", 1, true);
    }

    /**
     * {@inheritDoc}
     */
    public void cancel(ServiceContext ctx) {
        System.out.println("Stopping Vehicle Service on node:" + ignite.cluster().localNode());

        // Some custom logic.
    }

    /**
     * {@inheritDoc}
     */
    public void addVehicle(Integer vehicleId, Vehicle vehicle) {
        vehiclesCache.put(vehicleId, vehicle);
    }

    /**
     * {@inheritDoc}
     */
    public Vehicle getVehicle(int vehicleId) {
        return vehiclesCache.get(vehicleId);
    }

    /**
     * {@inheritDoc}
     */
    public void removeVehicle(int vehicleId) {
        vehiclesCache.remove(vehicleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer addVehicle(Vehicle vehicle) {
        long pk = sequence.getAndIncrement();
        vehicle.setId((int) pk);
        vehiclesCache.put(vehicle.getId(), vehicle);
        return vehicle.getId();
    }
}
