/*
 * ODPIndex.java
 * 
 * Created on 2004-06-27
 */
package com.stachoodev.carrot.odp;

import java.io.*;
import java.util.*;

import com.stachoodev.carrot.odp.index.*;

/**
 * This singleton provides centralized access to the ODP indexing
 * infrastructure. Use this class to discover existing indices, retrieve topic
 * locations and topic contents. Note: this class is not guaranteed to be
 * thread-safe.
 * 
 * @author stachoo
 */
public class ODPIndex
{
    /** An instance of TopicSerializer */
    private static final TopicSerializer topicSerializer;

    /** An instance of TopicIndexSerializer */
    private static final TopicIndexSerializer topicIndexSerializer;

    /** The PrimaryTopicIndex */
    private static PrimaryTopicIndex primaryTopicIndex;
    public static final String PRIMARY_TOPIC_INDEX_NAME = "primary";

    /** A map of all available topic indices (values) arranged by name (key) */
    private static final Map topicIndices;
    private static final List topicIndexNames;
    private static final String [] indexNames = new String [] {
    // No extra indices as yet
    };

    /** Location of all the data */
    private static String dataLocation;

    /** Initialize some stuff here */
    static
    {
        topicSerializer = new ZipFilesTopicSerializer();
        topicIndexSerializer = new ZipFileTopicIndexSerializer();
        topicIndices = new HashMap();
        topicIndexNames = Arrays.asList(indexNames);
    }

    /**
     * No public constructor.
     */
    private ODPIndex()
    {
    }

    /**
     * Initializes this singleton. Note that this method must be called before
     * any indices are retrieved. Additionally it can not be called more than
     * once.
     * 
     * @param indexDataLocation
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static synchronized void initialize(String indexDataLocation)
        throws IOException, ClassNotFoundException
    {
        if (dataLocation == null)
        {
            dataLocation = new File(indexDataLocation).getAbsolutePath();
            primaryTopicIndex = (PrimaryTopicIndex) topicIndexSerializer
                .deserialize(dataLocation
                    + System.getProperty("file.separator")
                    + PRIMARY_TOPIC_INDEX_NAME);
        }
        else
        {
            throw new RuntimeException("ODP index already initialized");
        }
    }

    /**
     * Returns the primary topic index. The ODPIndex must be initialized (see
     * {@link #initialize(String)}) before calling this method.
     * 
     * @return
     */
    public static PrimaryTopicIndex getPrimaryTopicIndex()
    {
        if (dataLocation == null)
        {
            throw new RuntimeException("ODPIndex not initialized");
        }

        return primaryTopicIndex;
    }

    /**
     * Returns names of all available ODP indices. The list does not include the
     * primary index (see {@link #getPrimaryTopicIndex()}.
     * 
     * @return
     */
    public static List getTopicIndexNames()
    {
        return topicIndexNames;
    }

    /**
     * The ODPIndex must be initialized (see {@link #initialize(String)})
     * before calling this method.
     * 
     * @param name
     * @return
     */
    public static TopicIndex getTopicIndex(String name)
    {
        if (dataLocation == null)
        {
            throw new RuntimeException("ODPIndex not initialized");
        }

        synchronized (ODPIndex.class)
        {
            if (!topicIndices.containsKey(name))
            {
                TopicIndex topicIndex = null;
                try
                {
                    topicIndex = topicIndexSerializer.deserialize(dataLocation
                        + System.getProperty("file.separator") + name);
                }
                catch (IOException ignored)
                {
                    // We will store null to indicate that the index is not
                    // available
                }
                catch (ClassNotFoundException ignored)
                {
                    // We will store null to indicate that the index is not
                    // available
                }
                topicIndices.put(name, topicIndex);
            }
        }

        return (TopicIndex) topicIndices.get(name);
    }

    /**
     * Returns the {@link TopicSerializer}that must be used to deserialize
     * topic objects from locations given by indices.
     * 
     * @return
     */
    public static TopicSerializer getTopicSerializer()
    {
        return topicSerializer;
    }

    /**
     * Returns the {@link TopicIndexSerializer}used by this singleton.
     * 
     * @return
     */
    public static TopicIndexSerializer getTopicIndexSerializer()
    {
        return topicIndexSerializer;
    }

    /**
     * A convenience method to deserializes an ODP topic from given relative
     * location. In case of any problems
     * <code>null<code> will be returned. The ODPIndex must be initialized (see
     * {@link #initialize(String)}) before calling this method.
     * 
     * @param location
     * @return
     */
    public static Topic getTopic(String location)
    {
        if (dataLocation == null)
        {
            throw new RuntimeException("ODPIndex not initialized");
        }

        try
        {
            return topicSerializer.deserialize(dataLocation
                + System.getProperty("file.separator") + location);
        }
        catch (IOException e)
        {
            return null;
        }
        catch (ClassNotFoundException e)
        {
            return null;
        }
    }
}