/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.directory.server.component.hub;


import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.apache.directory.server.component.ADSComponent;
import org.apache.directory.server.component.hub.listener.HubListener;
import org.apache.directory.server.component.instance.ADSComponentInstance;
import org.apache.directory.server.component.instance.DefaultComponentInstanceGenerator;
import org.apache.directory.server.component.schema.DefaultComponentSchemaGenerator;
import org.apache.directory.server.component.utilities.ADSComponentHelper;
import org.apache.directory.server.component.utilities.ADSConstants;
import org.apache.directory.server.component.utilities.ADSOSGIEventsHelper;
import org.apache.directory.server.component.utilities.EntryNormalizer;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.core.api.interceptor.Interceptor;
import org.apache.directory.server.core.api.partition.Partition;
import org.apache.directory.server.core.api.schema.SchemaPartition;
import org.apache.directory.server.core.partition.ldif.SingleFileLdifPartition;
import org.apache.directory.shared.ipojo.helpers.IPojoHelper;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.felix.ipojo.whiteboard.Wbp;
import org.apache.felix.ipojo.whiteboard.Whiteboards;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.log.LogService;


/**
 * An IPojo component that listens for incoming factories and instances.
 * Creating or destroying corresponding ADSComponent from them.
 * 
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@Component(name = ADSConstants.ADS_HUB_FACTORY_NAME)
@Whiteboards(whiteboards =
    {
        @Wbp(onArrival = "onFactoryArrival",
            onDeparture = "onFactoryDeparture",
            filter = "(objectClass=org.apache.felix.ipojo.Factory)"),
        @Wbp(onArrival = "onInstanceArrival",
            onDeparture = "onInstanceDeparture",
            filter = "(objectClass=org.apache.felix.ipojo.architecture.Architecture)")
})
public class ComponentHub implements EventHandler
{

    /*
     * BundleContext reference for event handling
     */
    public static BundleContext bundleContext;

    /*
     * DirectoryService reference
     */
    private DirectoryService ads;

    /*
     * Relative path name of the ApacheDS instance
     */
    private String instanceDir;

    /*
     * Schema Partition reference.
     */
    private SchemaPartition schemaPartition;

    /*
     * Config Partition reference
     */
    private SingleFileLdifPartition configPartition;

    /*
     * Used to set and get ADSComponent references.
     */
    private ComponentRegistry componentRegistry = new ComponentRegistry();

    /*
     * Used to manage listeners.
     */
    private ComponentEventManager eventManager = new ComponentEventManager();

    /*
     * Used to manage component's schemas.
     */
    private ComponentSchemaManager componentSchemaManager;

    /*
     * Used to manage config partition interactions.
     */
    private ConfigurationManager configManager;

    /*
     * Used to manage instances' DIT hooks.
     */
    private InstanceManager instanceManager = new InstanceManager( componentRegistry, eventManager );

    /*
     * Used to manage components.
     */
    private ComponentManager componentManager;

    /*
     * Allowed interfaces for components.
     */
    private String[] allowedInterfaces = new String[]
        { Interceptor.class.getName() };

    /*
     * OSGI Logger
     */
    @Requires
    private LogService logger;


    /**
     * Creates an IPojo component for the ComponentHub class.
     *
     * @param schemaPartition schema partition reference
     * @param configPartition config partition reference
     * @return reference to a wrapped ComponentHub IPojo component
     */
    public static ComponentHub createIPojoInstance( String instanceDir, SchemaPartition schemaPartition,
        SingleFileLdifPartition configPartition )
    {
        String hubInstanceId = ADSConstants.ADS_HUB_FACTORY_NAME + "-" + instanceDir;
        Properties conf = new Properties();

        conf.put( "ads-hub-arg-insdir", instanceDir );
        conf.put( "ads-hub-arg-schpart", schemaPartition );
        conf.put( "ads-hub-arg-confpart", configPartition );

        ComponentHub componentHubInstance = ( ComponentHub ) IPojoHelper.createIPojoComponent(
            ADSConstants.ADS_HUB_FACTORY_NAME, hubInstanceId, conf );

        return componentHubInstance;
    }


    public ComponentHub(
        @Property(name = "ads-hub-arg-insdir") String instanceDir,
        @Property(name = "ads-hub-arg-schpart") SchemaPartition schemaPartition,
        @Property(name = "ads-hub-arg-confpart") SingleFileLdifPartition configPartition )
    {
        this.instanceDir = instanceDir;
        this.schemaPartition = schemaPartition;
        this.configPartition = configPartition;

        componentSchemaManager = new ComponentSchemaManager( schemaPartition );
        configManager = new ConfigurationManager( configPartition, componentSchemaManager );
        componentManager = new ComponentManager( configManager );

        // Adding default schema generators for component types those will be managed
        componentSchemaManager.addSchemaGenerator( ADSConstants.ADS_COMPONENT_TYPE_INTERCEPTOR,
            new DefaultComponentSchemaGenerator() );
        componentSchemaManager.addSchemaGenerator( ADSConstants.ADS_COMPONENT_TYPE_PARTITION,
            new DefaultComponentSchemaGenerator() );
        componentSchemaManager.addSchemaGenerator( ADSConstants.ADS_COMPONENT_TYPE_SERVER,
            new DefaultComponentSchemaGenerator() );

        // Adding default instance generators for component types those will be managed
        componentManager.addInstanceGenerator( ADSConstants.ADS_COMPONENT_TYPE_INTERCEPTOR,
            new DefaultComponentInstanceGenerator() );
        componentManager.addInstanceGenerator( ADSConstants.ADS_COMPONENT_TYPE_PARTITION,
            new DefaultComponentInstanceGenerator() );
        componentManager.addInstanceGenerator( ADSConstants.ADS_COMPONENT_TYPE_SERVER,
            new DefaultComponentInstanceGenerator() );
    }


    /**
     * Register the DirectoryListener of InstanceManager with ADS
     *
     */
    private void RegisterWithDS()
    {
        instanceManager.registerWithDirectoryService( ads );
    }


    /**
     * Called when ADSComponentHub instance is validated by IPojo
     *
     */
    @Validate
    public void hubValidated()
    {
        logger.log( LogService.LOG_INFO, "ADSComponentHub validated." );

        // Register the class for OSGI Event handling
        String[] topics = new String[]
            {
                ADSOSGIEventsHelper.getTopic_DSInitialized( instanceDir )
        };

        Dictionary props = new Hashtable();
        props.put( EventConstants.EVENT_TOPIC, topics );

        bundleContext.registerService( EventHandler.class.getName(), this, props );
    }


    /**
     * Called when ADSComponentHub instance is invalidated by IPojo
     *
     */
    @Invalidate
    public void hubInvalidated()
    {
        logger.log( LogService.LOG_INFO, "ADSComponentHub being invalidated." );
    }


    /**
     * Factory arrival callback, registered by whiteboard handler.
     *
     * @param ref Reference to IPojo Factory
     */
    public void onFactoryArrival( ServiceReference ref )
    {
        Factory arrivingFactory = ( Factory ) ref.getBundle().getBundleContext().getService( ref );
        if ( !checkIfADSComponent( arrivingFactory ) )
        {
            return;
        }

        String componentType = parseComponentType( arrivingFactory );

        //Actual ADSComponent creation
        ADSComponent component = generateADSComponent( componentType, arrivingFactory );

        // Fire 'Component Created' event
        eventManager.fireComponentCreated( component );

        // Add the newly created component reference to registries
        componentRegistry.addComponent( component );
    }


    /**
     * Factory departure callback, registered by whiteboard handler.
     *
     * @param ref Reference to IPojo Factory
     */
    public void onFactoryDeparture( ServiceReference ref )
    {
        Factory leavingFactory = ( Factory ) ref.getBundle().getBundleContext().getService( ref );
        if ( !checkIfADSComponent( leavingFactory ) )
        {
            return;
        }

        String componentType = parseComponentType( leavingFactory );

        ADSComponent associatedComp = null;

        for ( ADSComponent _comp : componentRegistry.getAllComponents() )
        {
            if ( _comp.getFactory().getName().equals( leavingFactory.getName() ) )
            {
                associatedComp = _comp;
                break;
            }
        }

        if ( associatedComp == null )
        {
            logger.log( LogService.LOG_INFO, "Couldn't found an associated ADSComponent for factory:"
                + leavingFactory.getName() );
            return;
        }

        // Fire "Component Deleted" event
        eventManager.fireComponentDeleted( associatedComp );

        // Remove the component reference from registries
        componentRegistry.removeComponent( associatedComp );

    }


    /**
     * IPojo instance arrival callback, registered by whiteboard handler.
     *
     * @param ref Reference to IPojo instance
     */
    public void onInstanceArrival( ServiceReference ref )
    {

    }


    /**
     * IPojo instance departure callback, registered by whiteboard handler.
     *
     * @param ref Reference to IPojo instance
     */
    public void onInstanceDeparture( ServiceReference ref )
    {

    }


    /**
     * Check whether the argument is ADSComponent annotated.
     *
     * @param factory
     * @return
     */
    private boolean checkIfADSComponent( Factory factory )
    {
        String implementingIface = parseBaseInterface( factory );
        for ( String iface : allowedInterfaces )
        {
            if ( iface.equals( implementingIface ) )
            {
                return true;
            }
        }

        return false;
    }


    /**
     * Gets the component type by provided specification of a component.
     *
     * @param factory to get its component type
     * @return component type as interface name.
     */
    private String parseComponentType( Factory factory )
    {
        String baseInterface = parseBaseInterface( factory ).toLowerCase();

        if ( baseInterface.contains( "." ) )
        {
            return baseInterface.substring( baseInterface.lastIndexOf( '.' ) + 1 );
        }
        else
        {
            return baseInterface;
        }
    }


    private String parseBaseInterface( Factory factory )
    {
        String[] publishedInterfaces = factory.getComponentDescription().getprovidedServiceSpecification();
        if ( publishedInterfaces.length == 0 )
        {
            return null;
        }

        return publishedInterfaces[publishedInterfaces.length - 1];

    }


    /**
     * Generates a new ADSComponent with its schema and cache handle. 
     *
     * @param componentType Type of a component being created
     * @param factory a factory reference to create a ADSComponent for.
     * @return
     */
    private ADSComponent generateADSComponent( String componentType, Factory factory )
    {
        ADSComponent component = new ADSComponent( componentManager );

        component.setFactory( factory );
        component.setComponentType( componentType );
        component.setComponentName( ADSComponentHelper.getComponentName( component.getFactory() ) );
        component.setComponentVersion( ADSComponentHelper.getComponentVersion( component ) );

        configManager.pairWithComponent( component );

        return component;
    }


    /**
     * Registers a HubListener for specified component type.
     *
     * @param componentType component type to get notifications for.
     * @param listener HubListener implementation
     */
    public void registerListener( String componentType, HubListener listener )
    {
        eventManager.registerListener( componentType, listener );
    }


    /**
     * Removes the specified listener from the notification chain.
     *
     * @param listener HubListener implementation
     */
    public void removeListener( HubListener listener )
    {
        eventManager.removeListener( listener );
    }


    /**
     * OSGI Event handler.
     * Used for pairing component-hub with the DirectoryService reference.
     */
    @Override
    public void handleEvent( Event event )
    {
        String topic = event.getTopic();
        if ( topic.equals( ADSOSGIEventsHelper.getTopic_DSInitialized( instanceDir ) ) )
        {
            DirectoryService createdDS = ( DirectoryService ) event.getProperty( ADSOSGIEventsHelper.ADS_EVENT_ARG_DS );
            if ( createdDS == null )
            {
                logger.log( LogService.LOG_INFO,
                    "DSInitialized event does not contain created DirectoryServiceReference" );
                return;
            }

            ads = createdDS;
            RegisterWithDS();
        }
    }
}