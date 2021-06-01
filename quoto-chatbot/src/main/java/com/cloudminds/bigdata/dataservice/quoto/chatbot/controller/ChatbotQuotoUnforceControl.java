package com.cloudminds.bigdata.dataservice.quoto.chatbot.controller;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.cloudminds.bigdata.dataservice.quoto.chatbot.redis.RedisUtil;

import apijson.framework.APIJSONController;
import apijson.orm.AbstractSQLConfig;
import apijson.orm.Parser;

@RestController
@RequestMapping("/chatbot/unForce/quoto")
public class ChatbotQuotoUnforceControl extends APIJSONController {
	@Autowired
	private RedisUtil redisUtil;
	String serviceName = "chatbot";

	@Override
	public Parser<Long> newParser(HttpSession session, apijson.RequestMethod method) {
		return super.newParser(session, method).setNeedVerify(false); // TODO 这里关闭校验，方便新手快速测试，实际线上项目建议开启
	}

	public String getData(String request, HttpSession session) {
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
				return valueS;
			}

		}
		String result = get(request, session);
		if (redisExce) {
			return result;
		}
		if (result.contains("\"code\":200,\"msg\":\"success\"")) {
			if (!redisUtil.hset(serviceName, item, result, 60)) {
				System.err.println(
						"\n\n\n redis数据存储失败,存储的value:" + result + "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n");
			}
		}

		return result;
	}

	@PostMapping(value = "default")
	public String getDefaultDataNoForce(@RequestBody String request, HttpSession session) {
		request = "{'@force':false,'@schema':'DEFAULT'," + request.substring(request.indexOf("{") + 1);
		return getData(request, session);
	}

	@PostMapping(value = "cms")
	public String getCmsDataNoForce(@RequestBody String request, HttpSession session) {
		request = "{'@force':false,'@schema':'CMS'," + request.substring(request.indexOf("{") + 1);
		return getData(request, session);
	}

	@PostMapping(value = "sv")
	public String getSvDataNoForce(@RequestBody String request, HttpSession session) {
		request = "{'@force':false," + request.substring(request.indexOf("{") + 1);
		return getData(request, session);
	}
}
