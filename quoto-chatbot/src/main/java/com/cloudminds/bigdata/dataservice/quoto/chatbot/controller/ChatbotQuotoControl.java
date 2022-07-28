package com.cloudminds.bigdata.dataservice.quoto.chatbot.controller;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import apijson.JSON;
import com.cloudminds.bigdata.dataservice.quoto.chatbot.service.SaveAccessHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.cloudminds.bigdata.dataservice.quoto.chatbot.redis.RedisUtil;

import apijson.entity.CommonResponse;
import apijson.entity.ConfigLoadResponse;
import apijson.framework.APIJSONController;
import apijson.framework.APIJSONParser;
import apijson.orm.AbstractSQLConfig;
import apijson.orm.Parser;

@RestController
@RequestMapping("/chatbot/quoto")
public class ChatbotQuotoControl extends APIJSONController {
	@Autowired
	private RedisUtil redisUtil;
	String serviceName = "chatbot";
	@Autowired
	private SaveAccessHistory saveAccessHistory;

	@Override
	public Parser<Long> newParser(HttpSession session, apijson.RequestMethod method) {
		return super.newParser(session, method).setNeedVerify(false); // TODO 这里关闭校验，方便新手快速测试，实际线上项目建议开启
	}

	public String getData(String request, HttpServletRequest httpServletRequest, String servicePath) {
		JSONObject response=new JSONObject();
		response.put("ok",false);
		response.put("code",401);
		//取表名
		List<String> tableNameList = new ArrayList<>();
		try {
			tableNameList = getTableNames(request);
			if(tableNameList.isEmpty()){
				response.put("msg","验证用户权限时解析表名出错,请检查请求参数是否合法!");
				return response.toString();
			}
		}catch (Exception e){
			response.put("msg","验证用户权限时解析表名出错,请检查请求参数是否合法!");
			return response.toString();
		}

		HttpSession session = httpServletRequest.getSession();
		String token = httpServletRequest.getHeader("token");
		//第一步取token
		if (token == null) {
			response.put("msg","token不能为空!");
			return response.toString();
		}
		//第二步验证token值对应的权限
		Object token_map_object=redisUtil.get(serviceName+"_token");
		if(token_map_object!=null){
			Map<String, String> token_map = JSONObject.parseObject(JSONObject.toJSONString(token_map_object),
					Map.class);
			if(token_map!=null){
				String tokenAccess=token_map.get(token);
				if(tokenAccess==null){
					response.put("msg","用户没有此表的访问权限,请联系管理员!");
					return response.toString();
				}
				if (!tokenAccess.equals("ALL")) {
					String[] tokenAccessList = tokenAccess.toString().split(",");
					for(String tableName:tableNameList) {
						boolean hasAccess = false;
						for (String tokenAccessValue : tokenAccessList) {
							if (tokenAccessValue.equals(servicePath + "." + tableName)) {
								hasAccess = true;
								break;
							}
						}
						if (!hasAccess) {
							response.put("msg","用户没有"+tableName+"表的访问权限,请联系管理员!");
							return response.toString();
						}
					}

				}
			}
		}

		// 从redis获取配置信息
		try {
			Object TABLE_KEY_MAP = redisUtil.get(serviceName + "_table_key_map");
			Object TABLE_COLUMN_MAP = redisUtil.get(serviceName + "_table_column_map");
			if (TABLE_KEY_MAP != null) {
				@SuppressWarnings("unchecked")
				Map<String, String> table_key_map = JSONObject.parseObject(JSONObject.toJSONString(TABLE_KEY_MAP),
						Map.class);
				if (table_key_map != null) {
					AbstractSQLConfig.TABLE_KEY_MAP.clear();
					AbstractSQLConfig.TABLE_KEY_MAP = table_key_map;
				}
			}
			if (TABLE_COLUMN_MAP != null) {
				@SuppressWarnings("unchecked")
				Map<String, Map<String, String>> table_column_map = JSONObject
						.parseObject(JSONObject.toJSONString(TABLE_COLUMN_MAP), Map.class);
				if (table_column_map != null) {
					AbstractSQLConfig.tableColumnMap.clear();
					AbstractSQLConfig.tableColumnMap = table_column_map;
				}
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		// 从redis获取查询数据
		if (request == null || request.equals("")) {
			return get(request, session);
		}
		String item = DigestUtils.md5DigestAsHex(request.getBytes(StandardCharsets.UTF_8));
		Object value = null;
		boolean redisExce = false;
		try {
			value = redisUtil.hget(serviceName, item);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			redisExce = true;
		}
		if (value != null) {
			String valueS = value.toString();
			if (!valueS.equals("")) {
				accessHistory(token,servicePath,tableNameList.toString(),valueS,session);
				JSONObject jsonResult=JSON.parseObject(valueS);
				jsonResult.remove("execute_sql");
				return jsonResult.toString();
			}

		}
		String result = get(request, session);
		accessHistory(token,servicePath,tableNameList.toString(),result,session);
		if (redisExce) {
			JSONObject jsonResult=JSON.parseObject(result);
			jsonResult.remove("execute_sql");
			return jsonResult.toString();
		}
		if (result.contains("\"code\":200,\"msg\":\"success\"")) {
			if (!redisUtil.hset(serviceName, item, result, 60)) {
				System.err.println(
						"\n\n\n redis数据存储失败,存储的value:" + result + "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n");
			}
		}
		JSONObject jsonResult=JSON.parseObject(result);
		jsonResult.remove("execute_sql");
		return jsonResult.toString();
	}

	public void accessHistory(String token,String service_path,String table_alias,String response, HttpSession session){
		saveAccessHistory.saveAccessHistory(token,service_path,table_alias,response,session);
		//Thread t = new Thread(new SaveAccessHistory(token,service_path,table_alias,response,session));
		//t.start();
	}

	@PostMapping(value = "oozie")
	public String getDefaultData(@RequestBody String request, HttpServletRequest session) {
		request = "{'@schema':'oozie'," + request.substring(request.indexOf("{") + 1);
		return getData(request, session,"oozie");
	}

	@PostMapping(value = "metastore")
	public String getCmsData(@RequestBody String request, HttpServletRequest session) {
		request = "{'@schema':'metastore'," + request.substring(request.indexOf("{") + 1);
		return getData(request, session,"metastore");
	}

   //刷新逻辑优化
	@GetMapping(value = "refreshConfig")
	public CommonResponse refush() {
		APIJSONParser abstractParser = new APIJSONParser();
		ConfigLoadResponse configLoadResponse = abstractParser.loadAliasConfig();
		if (configLoadResponse.isSuccess()) {
			if (configLoadResponse.getTABLE_KEY_MAP() != null) {
				redisUtil.set(serviceName + "_table_key_map", configLoadResponse.getTABLE_KEY_MAP());
			}
			if (configLoadResponse.getTableColumnMap() != null) {
				redisUtil.set(serviceName + "_table_column_map", configLoadResponse.getTableColumnMap());
			}
		}
		CommonResponse commonResponse=new CommonResponse();
		commonResponse.setData(configLoadResponse.getData());
		commonResponse.setMessage(configLoadResponse.getMessage());
		commonResponse.setSuccess(configLoadResponse.isSuccess());
		return commonResponse;
	}

}
