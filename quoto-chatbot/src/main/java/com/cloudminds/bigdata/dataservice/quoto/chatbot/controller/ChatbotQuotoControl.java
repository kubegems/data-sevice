package com.cloudminds.bigdata.dataservice.quoto.chatbot.controller;

import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudminds.bigdata.dataservice.quoto.chatbot.redis.RedisUtil;

import apijson.entity.CommonResponse;
import apijson.framework.APIJSONController;
import apijson.framework.APIJSONParser;
import apijson.orm.Parser;

@RestController
@RequestMapping("/chatbot/quoto")
public class ChatbotQuotoControl extends APIJSONController {
	@Autowired
	private RedisUtil redisUtil;

	@Override
	public Parser<Long> newParser(HttpSession session, apijson.RequestMethod method) {
		return super.newParser(session, method).setNeedVerify(false); // TODO 这里关闭校验，方便新手快速测试，实际线上项目建议开启
	}

	public String getData(String request, HttpSession session) {
		String serviceName = "chatbot";
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
	public String getDefaultData(@RequestBody String request, HttpSession session) {
		request="{'@schema':'DEFAULT',"+request.substring(request.indexOf("{")+1);
		return getData(request, session);
	}
	
	@PostMapping(value = "cms")
	public String getCmsData(@RequestBody String request, HttpSession session) {
		request="{'@schema':'CMS',"+request.substring(request.indexOf("{")+1);
		return getData(request, session);
	}
	
	@PostMapping(value = "sv")
	public String getSvData(@RequestBody String request, HttpSession session) {
		return getData(request, session);
	}

	@GetMapping(value = "refreshConfig")
	public CommonResponse refush() {
		APIJSONParser abstractParser = new APIJSONParser();
		return abstractParser.loadAliasConfig();
	}

}
