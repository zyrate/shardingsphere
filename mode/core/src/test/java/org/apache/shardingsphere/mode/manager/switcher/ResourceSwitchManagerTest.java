/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.mode.manager.switcher;

import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ResourceSwitchManagerTest {
    
    @Test
    public void assertCreate() throws InterruptedException {
        Map<String, DataSource> dataSourceMap = createDataSourceMap();
        SwitchingResource actual = new ResourceSwitchManager().create(new ShardingSphereResourceMetaData("sharding_db", dataSourceMap), createToBeChangedDataSourcePropsMap());
        assertNewDataSources(actual);
        actual.closeStaleDataSources();
        assertStaleDataSources(dataSourceMap);
    }
    
    @Test
    public void assertCreateByAlterDataSourceProps() throws InterruptedException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(3, 1);
        dataSourceMap.put("ds_0", new MockedDataSource());
        dataSourceMap.put("ds_1", new MockedDataSource());
        SwitchingResource actual = new ResourceSwitchManager().createByAlterDataSourceProps(new ShardingSphereResourceMetaData("sharding_db", dataSourceMap), Collections.emptyMap());
        assertTrue(actual.getNewDataSources().isEmpty());
        assertThat(actual.getStaleDataSources().size(), is(2));
        actual.closeStaleDataSources();
        assertStaleDataSource((MockedDataSource) dataSourceMap.get("ds_0"));
        assertStaleDataSource((MockedDataSource) dataSourceMap.get("ds_1"));
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(3, 1);
        result.put("not_change", new MockedDataSource());
        result.put("replace", new MockedDataSource());
        return result;
    }
    
    private Map<String, DataSourceProperties> createToBeChangedDataSourcePropsMap() {
        Map<String, DataSourceProperties> result = new HashMap<>(3, 1);
        result.put("new", new DataSourceProperties(MockedDataSource.class.getName(), Collections.emptyMap()));
        result.put("not_change", new DataSourceProperties(MockedDataSource.class.getName(), Collections.emptyMap()));
        result.put("replace", new DataSourceProperties(MockedDataSource.class.getName(), Collections.singletonMap("password", "new_pwd")));
        return result;
    }
    
    private void assertNewDataSources(final SwitchingResource actual) {
        assertThat(actual.getNewDataSources().size(), is(3));
        assertTrue(actual.getNewDataSources().containsKey("not_change"));
        assertTrue(actual.getNewDataSources().containsKey("new"));
        assertTrue(actual.getNewDataSources().containsKey("replace"));
    }
    
    private void assertStaleDataSources(final Map<String, DataSource> originalDataSourceMap) throws InterruptedException {
        assertStaleDataSource((MockedDataSource) originalDataSourceMap.get("replace"));
        assertNotStaleDataSource((MockedDataSource) originalDataSourceMap.get("not_change"));
    }
    
    @SuppressWarnings("BusyWait")
    private void assertStaleDataSource(final MockedDataSource dataSource) throws InterruptedException {
        while (!dataSource.isClosed()) {
            Thread.sleep(10L);
        }
        assertTrue(dataSource.isClosed());
    }
    
    private void assertNotStaleDataSource(final MockedDataSource dataSource) {
        assertFalse(dataSource.isClosed());
    }
}
