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

package org.apache.shardingsphere.sharding.route.engine.validator.dml;

import org.apache.shardingsphere.infra.binder.statement.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.exception.syntax.DMLWithMultipleShardingTablesException;
import org.apache.shardingsphere.sharding.route.engine.validator.dml.impl.ShardingDeleteStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerDeleteStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public final class ShardingDeleteStatementValidatorTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Test
    public void assertPreValidateWhenDeleteMultiTablesForMySQL() {
        assertThrows(DMLWithMultipleShardingTablesException.class, () -> assertPreValidateWhenDeleteMultiTables(new MySQLDeleteStatement()));
    }
    
    @Test
    public void assertPreValidateWhenDeleteMultiTablesForOracle() {
        assertThrows(DMLWithMultipleShardingTablesException.class, () -> assertPreValidateWhenDeleteMultiTables(new OracleDeleteStatement()));
    }
    
    @Test
    public void assertPreValidateWhenDeleteMultiTablesForPostgreSQL() {
        assertThrows(DMLWithMultipleShardingTablesException.class, () -> assertPreValidateWhenDeleteMultiTables(new PostgreSQLDeleteStatement()));
    }
    
    @Test
    public void assertPreValidateWhenDeleteMultiTablesForSQL92() {
        assertThrows(DMLWithMultipleShardingTablesException.class, () -> assertPreValidateWhenDeleteMultiTables(new SQL92DeleteStatement()));
    }
    
    @Test
    public void assertPreValidateWhenDeleteMultiTablesForSQLServer() {
        assertThrows(DMLWithMultipleShardingTablesException.class, () -> assertPreValidateWhenDeleteMultiTables(new SQLServerDeleteStatement()));
    }
    
    private void assertPreValidateWhenDeleteMultiTables(final DeleteStatement sqlStatement) {
        DeleteMultiTableSegment tableSegment = new DeleteMultiTableSegment();
        tableSegment.getActualDeleteTables().add(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("user"))));
        tableSegment.getActualDeleteTables().add(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("order"))));
        tableSegment.getActualDeleteTables().add(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("order_item"))));
        sqlStatement.setTable(tableSegment);
        DeleteStatementContext sqlStatementContext = new DeleteStatementContext(sqlStatement);
        Collection<String> tableNames = new HashSet<>(Arrays.asList("user", "order", "order_item"));
        when(shardingRule.isAllShardingTables(tableNames)).thenReturn(false);
        when(shardingRule.tableRuleExists(tableNames)).thenReturn(true);
        new ShardingDeleteStatementValidator().preValidate(shardingRule, sqlStatementContext, Collections.emptyList(), mock(ShardingSphereDatabase.class), mock(ConfigurationProperties.class));
    }
}
