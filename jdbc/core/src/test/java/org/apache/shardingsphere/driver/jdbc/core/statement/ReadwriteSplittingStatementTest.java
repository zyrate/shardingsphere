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

package org.apache.shardingsphere.driver.jdbc.core.statement;

import org.apache.shardingsphere.driver.jdbc.base.AbstractShardingSphereDataSourceForReadwriteSplittingTest;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public final class ReadwriteSplittingStatementTest extends AbstractShardingSphereDataSourceForReadwriteSplittingTest {
    
    @Test
    public void assertQueryWithNull() throws SQLException {
        try (Statement statement = getReadwriteSplittingDataSource().getConnection().createStatement()) {
            assertThrows(SQLException.class, () -> statement.executeQuery(null));
        }
    }
    
    @Test
    public void assertQueryWithEmptyString() throws SQLException {
        try (Statement statement = getReadwriteSplittingDataSource().getConnection().createStatement()) {
            assertThrows(SQLException.class, () -> statement.executeQuery(""));
        }
    }
    
    @Test
    public void assertGetGeneratedKeys() throws SQLException {
        try (Statement statement = getReadwriteSplittingDataSource().getConnection().createStatement()) {
            statement.executeUpdate("INSERT INTO t_config(status) VALUES('OK');", Statement.RETURN_GENERATED_KEYS);
            ResultSet generatedKeys = statement.getGeneratedKeys();
            assertTrue(generatedKeys.next());
            int columnCount = generatedKeys.getMetaData().getColumnCount();
            for (int index = 0; index < columnCount; index++) {
                assertNotNull(generatedKeys.getObject(index + 1));
                assertNotNull(generatedKeys.getMetaData().getColumnLabel(index + 1));
                assertNotNull(generatedKeys.getMetaData().getColumnName(index + 1));
            }
            assertFalse(generatedKeys.next());
        }
    }
}
