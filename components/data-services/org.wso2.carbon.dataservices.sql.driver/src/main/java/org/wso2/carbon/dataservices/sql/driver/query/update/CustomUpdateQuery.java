/*
 *  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.dataservices.sql.driver.query.update;

import org.wso2.carbon.dataservices.sql.driver.TCustomConnection;
import org.wso2.carbon.dataservices.sql.driver.processor.reader.DataRow;
import org.wso2.carbon.dataservices.sql.driver.processor.reader.DataTable;
import org.wso2.carbon.dataservices.sql.driver.query.ColumnInfo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/**
 * This class represents a select query for custom data sources.
 */
public class CustomUpdateQuery extends UpdateQuery {

	public CustomUpdateQuery(Statement stmt) throws SQLException {
		super(stmt);
	}

	@Override
	public ResultSet executeQuery() throws SQLException {
		this.executeUpdate();
		return null;
	}

	@Override
	public int executeUpdate() throws SQLException {
		if (!(this.getConnection() instanceof TCustomConnection)) {
            throw new SQLException("Connection does not refer to a Custom connection");
        }
		DataTable table = ((TCustomConnection) this.getConnection()).getDataSource().getDataTable(
				this.getTargetTableName());
		if (table == null) {
			throw new SQLException("The custom data table '" + 
					this.getTargetTableName() + "' does not exist");
		}
		Map<Integer, DataRow> result;
        if (getCondition().getLhs() == null && getCondition().getRhs() == null) {
            result = getTargetTable().getRows();
        } else {
            result = getCondition().process(getTargetTable());
        }
        DataRow row;
        for (Map.Entry<Integer, DataRow> entry : result.entrySet()) {
        	row = entry.getValue();
    		for (ColumnInfo column : this.getTargetColumns()) {
    			if (column == null) {
    				continue;
    			}
    			row.getCell(table.getHeader(column.getName()).getId()).setCellValue(
    					this.findParam(column.getOrdinal()).getValue());
    		}
    		table.updateRows(row);
        }
		return result.keySet().size();
	}

	@Override
	public boolean execute() throws SQLException {
		this.executeUpdate();
		return true;
	}

}
