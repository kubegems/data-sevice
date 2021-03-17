package com.cloudminds.bigdata.dataservice.quoto.config.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cloudminds.bigdata.dataservice.quoto.config.mapper.ColumnAliasMapper;
import com.cloudminds.bigdata.dataservice.quoto.config.mapper.DatabaseInfoMapper;
import com.cloudminds.bigdata.dataservice.quoto.config.mapper.QuotoInfoMapper;
import com.cloudminds.bigdata.dataservice.quoto.config.mapper.TableInfoMapper;
import com.cloudminds.bigdata.dataservice.quoto.config.entity.ColumnAlias;
import com.cloudminds.bigdata.dataservice.quoto.config.entity.CommonResponse;
import com.cloudminds.bigdata.dataservice.quoto.config.entity.DatabaseInfo;
import com.cloudminds.bigdata.dataservice.quoto.config.entity.DbConnInfo;
import com.cloudminds.bigdata.dataservice.quoto.config.entity.QuotoInfo;
import com.cloudminds.bigdata.dataservice.quoto.config.entity.TableInfo;

@Service
public class DataServiceConfig {
	@Autowired
	private ColumnAliasMapper columnAliasMapper;
	@Autowired
	private DatabaseInfoMapper databaseInfoMapper;
	@Autowired
	private QuotoInfoMapper quotoInfoMapper;
	@Autowired
	private TableInfoMapper tableInfoMapper;

	// columnAlias
	public CommonResponse getColumnAlias(int tableId) {
		CommonResponse commonResponse = new CommonResponse();
		commonResponse.setData(columnAliasMapper.getColumnAliasByTableId(tableId));
		return commonResponse;
	}

	public CommonResponse updateColumnAliasStatus(int id, int status) {
		CommonResponse commonResponse = new CommonResponse();
		if (columnAliasMapper.updateColumnAliasStatus(id, status) != 1) {
			commonResponse.setMessage("更新失败,请稍后再试！");
			commonResponse.setSuccess(false);
		}
		return commonResponse;
	}

	public CommonResponse deleteColumnAlias(int id) {
		CommonResponse commonResponse = new CommonResponse();
		if (columnAliasMapper.updateColumnAliasDelete(id, 1) != 1) {
			commonResponse.setMessage("删除失败,请稍后再试！");
			commonResponse.setSuccess(false);
		}
		return commonResponse;
	}

	public CommonResponse insertColumnAlias(ColumnAlias columnAlias) {
		CommonResponse commonResponse = new CommonResponse();
		ColumnAlias columnAliasOld = columnAliasMapper.getColumnAlias(columnAlias);
		if (columnAliasOld != null) {
			if (columnAliasOld.getIs_delete() == 0) {
				commonResponse.setMessage("数据已存在,请不要重复新增！");
				commonResponse.setSuccess(false);
			} else {
				columnAliasOld.setDes(columnAlias.getDes());
				columnAliasOld.setColumn_alias(columnAlias.getColumn_alias());
				if (columnAliasMapper.updateColumnAlias(columnAliasOld) != 1) {
					commonResponse.setMessage("新增数据失败,请稍后再试！");
					commonResponse.setSuccess(false);
				}
			}
		} else {
			String dataType=getColunmType(columnAlias.getTable_id(),columnAlias.getColumn_name());
			if(dataType==null) {
				commonResponse.setMessage("列不存在或者查询列的数据类型失败！");
				commonResponse.setSuccess(false);
				return commonResponse;
			}
			columnAlias.setData_type(dataType);
			if (columnAliasMapper.insertColumnAlias(columnAlias) != 1) {
				commonResponse.setMessage("新增数据失败,请稍后再试！");
				commonResponse.setSuccess(false);
			}
		}
		return commonResponse;
	}

	public CommonResponse updateColumnAlias(ColumnAlias columnAlias) {
		CommonResponse commonResponse = new CommonResponse();
		String dataType=getColunmType(columnAliasMapper.getColumnAliasById(columnAlias.getId()).getTable_id(),columnAlias.getColumn_name());
		if(dataType==null) {
			commonResponse.setMessage("列不存在或者查询列的数据类型失败！");
			commonResponse.setSuccess(false);
			return commonResponse;
		}
		columnAlias.setData_type(dataType);
		if (columnAliasMapper.updateColumnAlias(columnAlias) != 1) {
			commonResponse.setMessage("更新失败,请稍后再试！");
			commonResponse.setSuccess(false);
		}
		return commonResponse;
	}

	// datainfo
	public CommonResponse getdbInfo() {
		CommonResponse commonResponse = new CommonResponse();
		commonResponse.setData(databaseInfoMapper.getdbInfo());
		return commonResponse;
	}

	// database
	public CommonResponse getDataBase() {
		CommonResponse commonResponse = new CommonResponse();
		commonResponse.setData(databaseInfoMapper.getDataBase());
		return commonResponse;
	}

	public CommonResponse getDataBaseBydbId(int dbId) {
		CommonResponse commonResponse = new CommonResponse();
		commonResponse.setData(databaseInfoMapper.getDataBaseByDbid(dbId));
		return commonResponse;
	}

	public CommonResponse updateDatabaseInfoStatus(int id, int status) {
		CommonResponse commonResponse = new CommonResponse();
		if (databaseInfoMapper.updateDatabaseInfoStatus(id, status) != 1) {
			commonResponse.setMessage("更新失败,请稍后再试！");
			commonResponse.setSuccess(false);
		}
		return commonResponse;
	}

	public CommonResponse deleteDatabaseInfo(int id) {
		CommonResponse commonResponse = new CommonResponse();
		if (databaseInfoMapper.updateDatabaseInfoDelete(id, 1) != 1) {
			commonResponse.setMessage("删除失败,请稍后再试！");
			commonResponse.setSuccess(false);
		}
		return commonResponse;
	}

	public CommonResponse insertDatabaseInfo(DatabaseInfo databaseInfo) {
		CommonResponse commonResponse = new CommonResponse();
		DatabaseInfo databaseInfoOld = databaseInfoMapper.getDatabaseInfo(databaseInfo);
		if (databaseInfoOld != null) {
			if (databaseInfoOld.getIs_delete() == 0) {
				commonResponse.setMessage("数据已存在,请不要重复新增！");
				commonResponse.setSuccess(false);
			} else {
				if (databaseInfoMapper.updateDatabaseInfoDelete(databaseInfoOld.getId(), 0) != 1) {
					commonResponse.setMessage("新增数据失败,请稍后再试！");
					commonResponse.setSuccess(false);
				}
			}
		} else {
			if (databaseInfoMapper.insertDatabaseInfo(databaseInfo) != 1) {
				commonResponse.setMessage("新增数据失败,请稍后再试！");
				commonResponse.setSuccess(false);
			}
		}
		return commonResponse;
	}

	// quotoInfo
	public CommonResponse getQuotoInfo(int tableId) {
		CommonResponse commonResponse = new CommonResponse();
		commonResponse.setData(quotoInfoMapper.getQuotoInfoByTableId(tableId));
		return commonResponse;
	}

	public CommonResponse updateQuotoInfoStatus(int id, int status) {
		CommonResponse commonResponse = new CommonResponse();
		if (quotoInfoMapper.updateQuotoInfoStatus(id, status) != 1) {
			commonResponse.setMessage("更新失败,请稍后再试！");
			commonResponse.setSuccess(false);
		}
		return commonResponse;
	}

	public CommonResponse deleteQuotoInfo(int id) {
		CommonResponse commonResponse = new CommonResponse();
		if (quotoInfoMapper.updateQuotoInfoDelete(id, 1) != 1) {
			commonResponse.setMessage("删除失败,请稍后再试！");
			commonResponse.setSuccess(false);
		}
		return commonResponse;
	}

	public CommonResponse insertQuotoInfo(QuotoInfo quotoInfo) {
		CommonResponse commonResponse = new CommonResponse();
		QuotoInfo quotoInfoOld = quotoInfoMapper.getQuotoInfo(quotoInfo);
		if (quotoInfoOld != null) {
			if (quotoInfoOld.getIs_delete() == 0) {
				commonResponse.setMessage("数据已存在,请不要重复新增！");
				commonResponse.setSuccess(false);
			} else {
				quotoInfoOld.setDes(quotoInfo.getDes());
				quotoInfoOld.setQuoto_sql(quotoInfo.getQuoto_sql());
				if (quotoInfoMapper.updateQuotoInfo(quotoInfoOld) != 1) {
					commonResponse.setMessage("新增数据失败,请稍后再试！");
					commonResponse.setSuccess(false);
				}
			}
		} else {
			if (quotoInfoMapper.insertQuotoInfo(quotoInfo) != 1) {
				commonResponse.setMessage("新增数据失败,请稍后再试！");
				commonResponse.setSuccess(false);
			}
		}
		return commonResponse;
	}

	public CommonResponse updateQuotoInfo(QuotoInfo quotoInfo) {
		CommonResponse commonResponse = new CommonResponse();
		if (quotoInfoMapper.updateQuotoInfo(quotoInfo) != 1) {
			commonResponse.setMessage("更新失败,请稍后再试！");
			commonResponse.setSuccess(false);
		}
		return commonResponse;
	}

	// tableInfo
	public CommonResponse getTableInfo(int databaseId) {
		CommonResponse commonResponse = new CommonResponse();
		commonResponse.setData(tableInfoMapper.getTableInfoByDataBaseId(databaseId));
		return commonResponse;
	}

	public CommonResponse updateTableInfoStatus(int id, int status) {
		CommonResponse commonResponse = new CommonResponse();
		if (tableInfoMapper.updateTableInfoStatus(id, status) != 1) {
			commonResponse.setMessage("更新失败,请稍后再试！");
			commonResponse.setSuccess(false);
		}
		return commonResponse;
	}

	public CommonResponse deleteTableInfo(int id) {
		CommonResponse commonResponse = new CommonResponse();
		if (tableInfoMapper.updateTableInfoDelete(id, 1) != 1) {
			commonResponse.setMessage("删除失败,请稍后再试！");
			commonResponse.setSuccess(false);
		}
		return commonResponse;
	}

	public CommonResponse insertTableInfo(TableInfo tableInfo) {
		CommonResponse commonResponse = new CommonResponse();
		TableInfo tableInfoOld = tableInfoMapper.getTableInfo(tableInfo);
		if (tableInfoOld != null) {
			if (tableInfoOld.getIs_delete() == 0) {
				commonResponse.setMessage("数据已存在,请不要重复新增！");
				commonResponse.setSuccess(false);
			} else {
				tableInfoOld.setTable_alias(tableInfo.getTable_alias());
				if (tableInfoMapper.updateTableInfo(tableInfoOld) != 1) {
					commonResponse.setMessage("新增数据失败,请稍后再试！");
					commonResponse.setSuccess(false);
				}
			}
		} else {
			if (tableInfoMapper.insertTableInfo(tableInfo) != 1) {
				commonResponse.setMessage("新增数据失败,请稍后再试！");
				commonResponse.setSuccess(false);
			}
		}
		return commonResponse;
	}

	public CommonResponse updateTableInfo(TableInfo tableInfo) {
		CommonResponse commonResponse = new CommonResponse();
		if (tableInfoMapper.updateTableInfo(tableInfo) != 1) {
			commonResponse.setMessage("更新失败,请稍后再试！");
			commonResponse.setSuccess(false);
		}
		return commonResponse;
	}

	public String getColunmType(int tableId, String columnName) {
		DbConnInfo dbConnInfo = databaseInfoMapper.getdbConnInfoByTableId(tableId);
		Connection conn = null;
		// 与数据库的连接
		PreparedStatement pStemt = null;
		try {
			conn = DriverManager.getConnection(dbConnInfo.getDb_url(), dbConnInfo.getUserName(), dbConnInfo.getPassword());
			pStemt = conn.prepareStatement("SELECT \""+columnName+"\" FROM \""+dbConnInfo.getDatabase()+"\".\""+dbConnInfo.getTable_name()+"\" limit 1");
			ResultSet set=pStemt.executeQuery();
			// 结果集元数据
			ResultSetMetaData rsmd=set.getMetaData();	
//			String dataType=rsmd.getColumnTypeName(1).toLowerCase();
//			StringUtils.trim(dataType);
//			if(dataType.contains("int")) {
//				return "int";
//			}else if(dataType.equals("float32")) {
//				return "float";
//			}else if(dataType.equals("float64")) {
//				return "double";
//			}else if(dataType.equals("date")) {
//				return "date";
//			}else if(dataType.equals("datetime")||dataType.equals("timestamp")) {
//				return "dateTime";
//			}else if(dataType.equals("varchar")) {
//				return "string";
//			}
			return rsmd.getColumnTypeName(1);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (pStemt != null) {
				try {
					pStemt.close();
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
		return null;
	}

}
