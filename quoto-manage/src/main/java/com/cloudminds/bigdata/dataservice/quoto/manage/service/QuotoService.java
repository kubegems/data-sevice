package com.cloudminds.bigdata.dataservice.quoto.manage.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.utils.StringUtils;
import com.cloudminds.bigdata.dataservice.quoto.manage.entity.Adjective;
import com.cloudminds.bigdata.dataservice.quoto.manage.entity.DataServiceResponse;
import com.cloudminds.bigdata.dataservice.quoto.manage.entity.Quoto;
import com.cloudminds.bigdata.dataservice.quoto.manage.entity.QuotoAccessHistory;
import com.cloudminds.bigdata.dataservice.quoto.manage.entity.QuotoInfo;
import com.cloudminds.bigdata.dataservice.quoto.manage.entity.ServicePathInfo;
import com.cloudminds.bigdata.dataservice.quoto.manage.entity.enums.StateEnum;
import com.cloudminds.bigdata.dataservice.quoto.manage.entity.enums.TypeEnum;
import com.cloudminds.bigdata.dataservice.quoto.manage.entity.request.BatchDeleteReq;
import com.cloudminds.bigdata.dataservice.quoto.manage.entity.request.CheckReq;
import com.cloudminds.bigdata.dataservice.quoto.manage.entity.request.DeleteReq;
import com.cloudminds.bigdata.dataservice.quoto.manage.entity.request.QuotoQuery;
import com.cloudminds.bigdata.dataservice.quoto.manage.entity.response.CommonQueryResponse;
import com.cloudminds.bigdata.dataservice.quoto.manage.entity.response.CommonResponse;
import com.cloudminds.bigdata.dataservice.quoto.manage.entity.response.DataCommonResponse;
import com.cloudminds.bigdata.dataservice.quoto.manage.mapper.QuotoAccessHistoryMapper;
import com.cloudminds.bigdata.dataservice.quoto.manage.mapper.QuotoMapper;
import com.cloudminds.bigdata.dataservice.quoto.manage.utils.DateTimeUtils;

@Service
public class QuotoService {
	@Autowired
	private QuotoMapper quotoMapper;
	@Autowired
	private QuotoAccessHistoryMapper quotoAccessHistoryMapper;
	@Value("${dataServiceUrl}")
	private String dataServiceUrl;
	@Autowired
	RestTemplate restTemplate;

	@SuppressWarnings("deprecation")
	public CommonResponse checkUnique(CheckReq checkReq) {
		// TODO Auto-generated method stub
		CommonResponse commonResponse = new CommonResponse();
		byte flag = checkReq.getCheckflag();
		Quoto quoto = null;
		if (checkReq.getCheckValue() == null || checkReq.getCheckValue().equals("")) {
			commonResponse.setSuccess(false);
			commonResponse.setMessage("check的值不能为空");
			return commonResponse;
		}
		if (flag == 0) {
			String checkValue = checkReq.getCheckValue();
			if (NumberUtils.isNumber(checkValue) || checkValue.contains("(") || checkValue.contains(")")
					|| checkValue.contains("+") || checkValue.contains("-") || checkValue.contains("*")
					|| checkValue.contains("/") || checkValue.contains("#")) {
				commonResponse.setSuccess(false);
				commonResponse.setMessage("名字不能是数或者含有()+-*/#特殊符号");
				return commonResponse;
			}
			quoto = quotoMapper.findQuotoByName(checkReq.getCheckValue());
		} else if (flag == 1) {
			quoto = quotoMapper.findQuotoByField(checkReq.getCheckValue());
		} else {
			commonResponse.setSuccess(false);
			commonResponse.setMessage("不支持check的类型");
			return commonResponse;
		}
		if (quoto != null) {
			commonResponse.setSuccess(false);
			commonResponse.setData(quoto);
			commonResponse.setMessage("已存在,请重新命名");
		}
		return commonResponse;
	}

	public CommonResponse queryAllBusiness() {
		// TODO Auto-generated method stub
		CommonResponse commonResponse = new CommonResponse();
		commonResponse.setData(quotoMapper.queryAllBusiness());
		return commonResponse;
	}

	public CommonResponse queryAllDataDomain(int businessId) {
		// TODO Auto-generated method stub
		CommonResponse commonResponse = new CommonResponse();
		commonResponse.setData(quotoMapper.queryAllDataDomain(businessId));
		return commonResponse;
	}

	public CommonResponse queryAllBusinessProcess(int dataDomainId) {
		// TODO Auto-generated method stub
		CommonResponse commonResponse = new CommonResponse();
		commonResponse.setData(quotoMapper.queryAllBusinessProcess(dataDomainId));
		return commonResponse;
	}

	public CommonResponse queryAllDataService() {
		// TODO Auto-generated method stub
		CommonResponse commonResponse = new CommonResponse();
		commonResponse.setData(quotoMapper.queryAllDataService());
		return commonResponse;
	}

	public CommonResponse queryAllDimension(int tableId) {
		// TODO Auto-generated method stub
		CommonResponse commonResponse = new CommonResponse();
		commonResponse.setData(quotoMapper.queryAllDimension(tableId));
		return commonResponse;
	}

	public CommonResponse deleteQuoto(DeleteReq deleteReq) {
		// TODO Auto-generated method stub
		CommonResponse commonResponse = new CommonResponse();
		// 查询指标
		Quoto quoto = quotoMapper.findQuotoById(deleteReq.getId());
		if (quoto == null) {
			commonResponse.setMessage("id为：" + deleteReq.getId() + "的指标不存");
			commonResponse.setSuccess(false);
			return commonResponse;
		}
		// 指标是原始指标，是否有被派生指标引用
		if (quoto.getType() == TypeEnum.atomic_quoto.getCode()) {
			List<String> quotoNames = quotoMapper.findQuotoNameByOriginQuoto(deleteReq.getId());
			if (quotoNames != null && quotoNames.size() > 0) {
				commonResponse.setMessage("衍生指标" + quotoNames.toString() + "在使用(" + quoto.getName() + ")指标,不能删除");
				commonResponse.setSuccess(false);
				return commonResponse;
			}
		}
		// 指标有没有被复合指标使用
		List<String> quotoNames = quotoMapper.findQuotoNameByContainQuotoId(quoto.getId());
		if (quotoNames != null && quotoNames.size() > 0) {
			commonResponse.setMessage("衍生指标" + quotoNames.toString() + "在使用(" + quoto.getName() + ")指标,不能删除");
			commonResponse.setSuccess(false);
			return commonResponse;
		}

		if (quotoMapper.deleteQuotoById(deleteReq.getId()) <= 0) {
			commonResponse.setMessage("指标(" + quoto.getName() + ")删除失败,请稍后再试");
			commonResponse.setSuccess(false);
		}
		return commonResponse;
	}

	public CommonResponse batchDeleteQuoto(BatchDeleteReq batchDeleteReq) {
		// TODO Auto-generated method stub
		CommonResponse commonResponse = new CommonResponse();
		if (batchDeleteReq.getIds() == null || batchDeleteReq.getIds().length == 0) {
			commonResponse.setMessage("删除的指标id不能为空");
			commonResponse.setSuccess(false);
			return commonResponse;
		}
		for (int i = 0; i < batchDeleteReq.getIds().length; i++) {
			DeleteReq deleteReq = new DeleteReq();
			deleteReq.setId(batchDeleteReq.getIds()[i]);
			CommonResponse commonResponseDelete = deleteQuoto(deleteReq);
			if (!commonResponseDelete.isSuccess()) {
				return commonResponseDelete;
			}
		}
		return commonResponse;
	}

	public CommonResponse insertQuoto(Quoto quoto) {
		// TODO Auto-generated method stub
		CommonResponse commonResponse = new CommonResponse();
		// 基础校验
		if (quoto == null || StringUtils.isEmpty(quoto.getName())) {
			commonResponse.setSuccess(false);
			commonResponse.setMessage("名字不能为空");
			return commonResponse;
		}

		if (StringUtils.isEmpty(quoto.getField())) {
			commonResponse.setSuccess(false);
			commonResponse.setMessage("字段名称不能为空");
			return commonResponse;
		}

		if (quotoMapper.findQuotoByName(quoto.getName()) != null) {
			commonResponse.setSuccess(false);
			commonResponse.setMessage("名字已存在,请重新命名");
			return commonResponse;
		}

		if (quotoMapper.findQuotoByField(quoto.getField()) != null) {
			commonResponse.setSuccess(false);
			commonResponse.setMessage("字段已存在,请重新命名");
			return commonResponse;
		}
		if (quoto.getType() == TypeEnum.atomic_quoto.getCode()) {
			if (quoto.getTable_id() == 0) {
				commonResponse.setSuccess(false);
				commonResponse.setMessage("数据服务不能为空");
				return commonResponse;
			}
			if (quoto.getCycle() == 0) {
				commonResponse.setSuccess(false);
				commonResponse.setMessage("计算周期不能为空");
				return commonResponse;
			}
		} else if (quoto.getType() == TypeEnum.derive_quoto.getCode()) {
			quoto.setState(StateEnum.active_state.getCode());
			if (quoto.getOrigin_quoto() <= 0) {
				commonResponse.setSuccess(false);
				commonResponse.setMessage("原子指标的id必须有值");
				return commonResponse;
			}
		} else {
			quoto.setState(StateEnum.active_state.getCode());
			if (StringUtils.isEmpty(quoto.getExpression())) {
				commonResponse.setSuccess(false);
				commonResponse.setMessage("加工方式必须有值");
				return commonResponse;
			}
			String expression = quoto.getExpression().replaceAll(" ", "").replaceAll("[(]", "").replaceAll("[)]", "")
					.replaceAll("[+]", " ").replaceAll("[-]", " ").replaceAll("[*]", " ").replaceAll("[/]", " ");
			String[] expressions = expression.split(" ");
			int[] quotos = new int[expressions.length];
			for (int i = 0; i < expressions.length; i++) {
				Quoto quotoInfo = quotoMapper.queryQuotoByName(expressions[i]);
				if (quotoInfo == null || quotoInfo.getState() != StateEnum.active_state.getCode()) {
					commonResponse.setSuccess(false);
					commonResponse.setMessage(expressions[i] + ":此指标不存在或未激活,请重新写");
					return commonResponse;
				}
				quotos[i] = quotoInfo.getId();
			}
			quoto.setQuotos(quotos);
		}

		// 插入数据库
		try {
			quotoMapper.insertQuoto(quoto);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			commonResponse.setSuccess(false);
			commonResponse.setMessage("数据插入失败,请稍后再试");
			return commonResponse;
		}
		return commonResponse;
	}

	public CommonResponse updateQuoto(Quoto quoto) {
		// TODO Auto-generated method stub
		CommonResponse commonResponse = new CommonResponse();
		Quoto oldQuoto = quotoMapper.queryQuotoById(quoto.getId());
		if (oldQuoto == null) {
			commonResponse.setSuccess(false);
			commonResponse.setMessage("指标不存在,请刷新列表界面后再试！");
			return commonResponse;
		}
		// 基础校验
		if (quoto == null || StringUtils.isEmpty(quoto.getName())) {
			commonResponse.setSuccess(false);
			commonResponse.setMessage("名字不能为空");
			return commonResponse;
		}

		if (StringUtils.isEmpty(quoto.getField())) {
			commonResponse.setSuccess(false);
			commonResponse.setMessage("字段名称不能为空");
			return commonResponse;
		}

		if (!quoto.getName().equals(oldQuoto.getName())) {
			if (quotoMapper.findQuotoByName(quoto.getName()) != null) {
				commonResponse.setSuccess(false);
				commonResponse.setMessage("名字已存在,请重新命名");
				return commonResponse;
			}
		}

		if (!quoto.getField().equals(oldQuoto.getField())) {
			if (quotoMapper.findQuotoByField(quoto.getField()) != null) {
				commonResponse.setSuccess(false);
				commonResponse.setMessage("字段已存在,请重新命名");
				return commonResponse;
			}
		}

		if (oldQuoto.getType() == TypeEnum.atomic_quoto.getCode()
				&& oldQuoto.getState() == StateEnum.active_state.getCode()) {
			if (!oldQuoto.getField().equals(quoto.getField())) {
				commonResponse.setSuccess(false);
				commonResponse.setMessage("激活的原子指标不能修改字段名！");
				return commonResponse;
			}
			if (oldQuoto.getTable_id() != quoto.getTable_id()) {
				commonResponse.setSuccess(false);
				commonResponse.setMessage("激活的原子指标不能修改数据服务！");
				return commonResponse;
			}
		}
		if (quoto.getType() == TypeEnum.complex_quoto.getCode()) {
			if (quoto.getExpression().equals(oldQuoto.getExpression())) {
				quoto.setQuotos(oldQuoto.getQuotos());
			} else {
				if (StringUtils.isEmpty(quoto.getExpression())) {
					commonResponse.setSuccess(false);
					commonResponse.setMessage("加工方式必须有值");
					return commonResponse;
				}
				String expression =  quoto.getExpression().replaceAll(" ", "").replaceAll("[(]", "").replaceAll("[)]", "")
						.replaceAll("[+]", " ").replaceAll("[-]", " ").replaceAll("[*]", " ").replaceAll("[/]", " ");
				String[] expressions = expression.split(" ");
				int[] quotos = new int[expressions.length];
				for (int i = 0; i < expressions.length; i++) {
					Quoto quotoInfo = quotoMapper.queryQuotoByName(expressions[i]);
					if (quotoInfo == null || quotoInfo.getState() != StateEnum.active_state.getCode()) {
						commonResponse.setSuccess(false);
						commonResponse.setMessage(expressions[i] + ":此指标不存在或未激活,请重新写");
						return commonResponse;
					}
					quotos[i] = quotoInfo.getId();
				}
				quoto.setQuotos(quotos);
			}
		}
		try {
			if (quotoMapper.updateQuoto(quoto) <= 0) {
				commonResponse.setSuccess(false);
				commonResponse.setMessage("编辑指标失败，请稍后再试！");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			commonResponse.setSuccess(false);
			commonResponse.setMessage("编辑指标失败，请稍后再试！");
		}
		return commonResponse;
	}

	public CommonQueryResponse queryQuoto(QuotoQuery quotoQuery) {
		// TODO Auto-generated method stub
		CommonQueryResponse commonQueryResponse = new CommonQueryResponse();
		String condition = "deleted=0";
		if (quotoQuery.getType() != -1) {
			condition = condition + " and type=" + quotoQuery.getType();
		}

		if (quotoQuery.getName() != null && (!quotoQuery.getName().equals(""))) {
			condition = condition + " and name like '" + quotoQuery.getName() + "%'";
		}

		if (quotoQuery.getField() != null && (!quotoQuery.getField().equals(""))) {
			condition = condition + " and field like '" + quotoQuery.getField() + "%'";
		}

		if (quotoQuery.getBusinessId() != -1) {
			condition = condition + " and business_id=" + quotoQuery.getBusinessId();
		}

		if (quotoQuery.getState() != -1) {
			condition = condition + " and state=" + quotoQuery.getState();
		}

		condition = condition + " order by update_time desc";
		int page = quotoQuery.getPage();
		int size = quotoQuery.getSize();
		int startLine = (page - 1) * size;
		commonQueryResponse.setData(quotoMapper.queryQuoto(condition, startLine, size));
		commonQueryResponse.setCurrentPage(quotoQuery.getPage());
		commonQueryResponse.setTotal(quotoMapper.queryQuotoCount(condition));
		return commonQueryResponse;
	}

	public CommonResponse queryAllQuoto(QuotoQuery quotoQuery) {
		// TODO Auto-generated method stub
		CommonResponse commonResponse = new CommonResponse();
		String condition = "deleted=0 and state=1";
		if (quotoQuery.getType() != -1) {
			condition = condition + " and type=" + quotoQuery.getType();
		}

		if (quotoQuery.getBusiness_process_id() != -1) {
			condition = condition + " and business_process_id=" + quotoQuery.getBusiness_process_id();
		}

		condition = condition + " order by name asc";

		commonResponse.setData(quotoMapper.queryAllQuoto(condition));
		return commonResponse;
	}

	public CommonResponse queryAllCycle() {
		// TODO Auto-generated method stub
		CommonResponse commonResponse = new CommonResponse();
		commonResponse.setData(quotoMapper.queryAllCycle());
		return commonResponse;
	}

	public CommonResponse queryQuotoById(int id) {
		// TODO Auto-generated method stub
		CommonResponse commonResponse = new CommonResponse();
		Quoto quoto = quotoMapper.queryQuotoById(id);
		if (quoto == null) {
			commonResponse.setSuccess(false);
			commonResponse.setMessage("指标不存在,请核实id值是否正确");
			return commonResponse;
		}
		commonResponse.setData(quotoMapper.queryQuotoById(id));
		return commonResponse;
	}

	public CommonResponse activeQuoto(int id) {
		// TODO Auto-generated method stub
		CommonResponse commonResponse = new CommonResponse();
		Quoto quoto = quotoMapper.findQuotoById(id);
		if (quoto == null) {
			commonResponse.setSuccess(false);
			commonResponse.setMessage("指标不存在,请核实id值是否正确");
			return commonResponse;
		}
		if (quoto.getType() != TypeEnum.atomic_quoto.getCode()) {
			commonResponse.setSuccess(false);
			commonResponse.setMessage("只有原子指标才有激活的操作");
			return commonResponse;
		}
		if (quoto.getState() == StateEnum.active_state.getCode()) {
			commonResponse.setSuccess(true);
			commonResponse.setMessage("指标已激活");
			return commonResponse;
		}

		QuotoInfo quotoInfo = quotoMapper.queryQuotoInfo(quoto.getField());
		if (quotoInfo == null) {
			commonResponse.setSuccess(false);
			commonResponse.setMessage("数据服务没有此指标的配置,请前往数据服务-服务管理页配置此指标");
			return commonResponse;
		}
		if (quotoInfo.getState() == 0) {
			commonResponse.setSuccess(false);
			commonResponse.setMessage("数据服务此指标不可用,请前往数据服务-服务管理页启用此指标");
			return commonResponse;
		}

		// 激活指标
		if (quotoMapper.updateQuotoState(StateEnum.active_state.getCode(), id) != 1) {
			commonResponse.setSuccess(false);
			commonResponse.setMessage("激活失败,请稍后再试");
			return commonResponse;
		}
		return commonResponse;
	}

	public CommonResponse queryFuzzy(QuotoQuery quotoQuery) {
		// TODO Auto-generated method stub
		CommonResponse commonResponse = new CommonResponse();
		String condition = "deleted=0";
		if (quotoQuery.getType() != -1) {
			condition = condition + " and type=" + quotoQuery.getType();
		}

		if (quotoQuery.getName() != null && (!quotoQuery.getName().equals(""))) {
			condition = condition + " and name like '" + quotoQuery.getName() + "%'";
		}
		commonResponse.setData(quotoMapper.queryQuotoFuzzy(condition));
		return commonResponse;
	}

	public DataCommonResponse queryQuotoData(Integer id, String quotoName, Integer page, Integer count) {
		// TODO Auto-generated method stub
		DataCommonResponse commonResponse = new DataCommonResponse();
		// 查询指标
		Quoto quoto = null;
		if (id != null && id > 0) {
			quoto = quotoMapper.queryQuotoById(id);
		}
		if (quoto == null && (!StringUtils.isEmpty(quotoName))) {
			quoto = quotoMapper.queryQuotoByName(quotoName);
		}
		if (page == null) {
			page = 0;
		}
		if (quoto == null) {
			commonResponse.setSuccess(false);
			commonResponse.setMessage("指标不存在");
			return commonResponse;
		}
		if (quoto.getState() != StateEnum.active_state.getCode()) {
			commonResponse.setSuccess(false);
			commonResponse.setMessage("非激活状态的指标不可以查询数据");
			return commonResponse;
		}
		if (count == null || count <= 0) {
			count = 1000;
		}

		if (count > 10000) {
			commonResponse.setSuccess(false);
			commonResponse.setMessage("count最大值为10000");
			return commonResponse;
		}
		commonResponse = queryDataFromDataService(quoto, page, count);
		QuotoAccessHistory quotoAccessHistory = new QuotoAccessHistory();
		quotoAccessHistory.setQuoto_id(quoto.getId());
		quotoAccessHistory.setQuoto_name(quoto.getName());
		quotoAccessHistory.setBusiness(quoto.getBusiness_name());
		quotoAccessHistory.setDomain(quoto.getData_domain_name());
		quotoAccessHistory.setProcess(quoto.getBusiness_process_name());
		quotoAccessHistory.setLevel(quoto.getQuoto_level());
		quotoAccessHistory.setType(quoto.getType());
		quotoAccessHistory.setSuccess(commonResponse.isSuccess());
		quotoAccessHistory.setMessage(commonResponse.getMessage());
		quotoAccessHistoryMapper.insertAccessHistory(quotoAccessHistory);
		if (quoto.getType() == TypeEnum.derive_quoto.getCode()) {
			Quoto atomicQuoto = quotoMapper.queryQuotoById(quoto.getOrigin_quoto());
			quotoAccessHistory.setQuoto_id(atomicQuoto.getId());
			quotoAccessHistory.setQuoto_name(atomicQuoto.getName());
			quotoAccessHistory.setBusiness(atomicQuoto.getBusiness_name());
			quotoAccessHistory.setDomain(atomicQuoto.getData_domain_name());
			quotoAccessHistory.setProcess(atomicQuoto.getBusiness_process_name());
			quotoAccessHistory.setLevel(atomicQuoto.getQuoto_level());
			quotoAccessHistory.setType(atomicQuoto.getType());
			quotoAccessHistory.setSuccess(commonResponse.isSuccess());
			quotoAccessHistory.setMessage(commonResponse.getMessage());
			quotoAccessHistoryMapper.insertAccessHistory(quotoAccessHistory);
		}
		return commonResponse;
	}

	public DataCommonResponse queryDataFromDataService(Quoto quoto, int page, int count) {
		DataCommonResponse commonResponse = new DataCommonResponse();

		// 复合指标处理逻辑
		if (quoto.getType() == TypeEnum.complex_quoto.getCode()) {
			try {
				DataCommonResponse dataCommonResponse = caculate(quoto.getExpression().replace(" ", "") + "#", page,
						count);
				if (dataCommonResponse.isSuccess()) {
					quoto.setCycle(dataCommonResponse.getCycle());
					quoto.setDimension(dataCommonResponse.getDimensionIds());
					quotoMapper.updateQuoto(quoto);
				}
				return dataCommonResponse;
			} catch (Exception e) {
				// TODO: handle exception
				commonResponse.setSuccess(false);
				commonResponse.setMessage(e.getMessage());
				return commonResponse;
			}
		}
		// 非复合指标处理逻辑
		Quoto atomicQuoto = quoto;
		if (quoto.getType() == TypeEnum.derive_quoto.getCode()) {
			atomicQuoto = quotoMapper.findQuotoById(quoto.getOrigin_quoto());
		}
		commonResponse.setField(atomicQuoto.getField());
		commonResponse.setCycle(atomicQuoto.getCycle());
		// 查询数据服务对应的信息
		ServicePathInfo servicePathInfo = quotoMapper.queryServicePathInfo(atomicQuoto.getTable_id());
		if (servicePathInfo == null) {
			commonResponse.setSuccess(false);
			commonResponse.setMessage("指标对应的服务不可用,请联系管理员排查");
			return commonResponse;
		}
		String url = dataServiceUrl + servicePathInfo.getPath();
		String bodyRequest = "{'[]':{'" + servicePathInfo.getTableName() + "':{'@column':'" + atomicQuoto.getField();
		if (quoto.getType() == TypeEnum.derive_quoto.getCode()) {
			// 添加维度的请求参数
			if (quoto.getDimension() != null && quoto.getDimension().length > 0) {
				String group = "'@group':'";
				bodyRequest = bodyRequest + ";";
				// 查询维度的名称
				Set<String> dimensionName = quotoMapper.queryDimensionName(quoto.getId());
				commonResponse.setDimensions(dimensionName);
				commonResponse.setDimensionIds(quoto.getDimension());
				int i = 0;
				for (String dimension : dimensionName) {
					if (i == dimensionName.size() - 1) {
						group = group + dimension + "'";
						bodyRequest = bodyRequest + dimension + "'";
					} else {
						group = group + dimension + ",";
						bodyRequest = bodyRequest + dimension + ";";
					}
					i++;
				}
				bodyRequest = bodyRequest + "," + group;
			} else {
				bodyRequest = bodyRequest + "'";
			}
			// 添加修饰词的请求参数
			if (quoto.getAdjective() != null && quoto.getAdjective().length > 0) {
				// 查询修饰词信息
				List<Adjective> adjectives = quotoMapper.queryAdjective(quoto.getId());
				for (int i = 0; i < adjectives.size(); i++) {
					bodyRequest = bodyRequest + "," + getAdjectiveReq(adjectives.get(i));
				}
			}
		} else {
			bodyRequest = bodyRequest + "'";
		}
		bodyRequest = bodyRequest + "},'page':" + page + ",'count':" + count + "}}";
		System.out.println(bodyRequest);
		// 请求数据服务
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		// 将请求头部和参数合成一个请求
		HttpEntity<String> requestEntity = new HttpEntity<>(bodyRequest, headers);
		ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, requestEntity, String.class);
		if (!responseEntity.getStatusCode().is2xxSuccessful()) {
			commonResponse.setSuccess(false);
			commonResponse.setMessage("指标对应的服务不可用,请联系管理员排查");
			return commonResponse;
		} else {
			JSONObject result = JSONObject.parseObject(responseEntity.getBody().toString());
			DataServiceResponse dataServiceResponse = JSONObject.toJavaObject(
					JSONObject.parseObject(responseEntity.getBody().toString()), DataServiceResponse.class);
			commonResponse.setSuccess(dataServiceResponse.isOk());
			commonResponse.setMessage(dataServiceResponse.getMsg());
			if (dataServiceResponse.isOk()) {
				if (result.get("[]") == null) {
					commonResponse.setType(0);
					return commonResponse;
				}
				List<JSONObject> list = JSONObject.parseArray(result.get("[]").toString(), JSONObject.class);
				if (list != null) {
					if (list.size() == 1) {
						commonResponse.setType(1);
						commonResponse.setData(list.get(0).get(servicePathInfo.getTableName()));
					} else {
						List<Object> data = new ArrayList<Object>();
						for (int i = 0; i < list.size(); i++) {
							data.add(list.get(i).get(servicePathInfo.getTableName()));
						}
						commonResponse.setType(2);
						commonResponse.setData(data);
					}
				}
			}
		}
		return commonResponse;
	}

	@SuppressWarnings("deprecation")
	private DataCommonResponse getCalculateValue(String fieldName, int page, int count) {
		if (NumberUtils.isNumber(fieldName)) {
			DataCommonResponse calculateValue = new DataCommonResponse();
			calculateValue.setData(new BigDecimal(fieldName));
			calculateValue.setType(3);
			return calculateValue;
		}
		DataCommonResponse commonResponse = queryQuotoData(0, fieldName, page, count);
		if (!commonResponse.isSuccess()) {
			// 抛异常
			throw new UnsupportedOperationException("指标(" + fieldName + ")获取失败：" + commonResponse.getMessage());
		} else {
			return commonResponse;
		}

	}

	private DataCommonResponse CalculateValue(DataCommonResponse a, DataCommonResponse b, String op) {
		int cycle = Math.max(a.getCycle(), b.getCycle());
		a.setCycle(cycle);
		b.setCycle(cycle);
		// a为0 代表a查出来没有数据
		if (a.getType() == 0) {
			if (op.equals("+")) {
				return b;
			} else if (op.equals("*")) {
				return a;
			} else if (op.equals("/")) {
				if (b.getType() != 0) {
					return a;
				} else {
					// 抛异常，除数的分母为0
					throw new UnsupportedOperationException("除法里分母为0,具体分母数据：" + b.getField() + "(" + b.getData() + ")");
				}
			} else { // 减法
				if (b.getType() == 0) {
					return a;
				} else {
					// 将b的数变成相反数
					if (b.getType() == 3) {
						b.setData(new BigDecimal(b.getData().toString()).multiply(new BigDecimal("-1")));
					} else if (b.getType() == 2) {
						List<JSONObject> list = JSONObject.parseArray(b.getData().toString(), JSONObject.class);
						for (int i = 0; i < list.size(); i++) {
							BigDecimal value = list.get(i).getBigDecimal(b.getField());
							list.get(i).put(b.getField(), value.multiply(new BigDecimal("-1")));
						}
						b.setData(list);
					} else {
						JSONObject object = JSONObject.parseObject(b.getData().toString());
						object.put(b.getField(), object.getBigDecimal(b.getField()).multiply(new BigDecimal("-1")));
						b.setData(object);
					}
					return b;
				}
			}
		}
		// a为3 代表从外面传进来的数
		if (a.getType() == 3) {
			BigDecimal aValue = new BigDecimal(a.getData().toString());
			if (b.getType() == 0) {
				if (op.equals("+") || op.equals("-")) {
					return a;
				} else if (op.equals("*")) {
					return b;
				} else {
					// 抛异常，除数的分母为0
					throw new UnsupportedOperationException("除法里分母为0,具体分母数据：" + b.getField() + "(" + b.getData() + ")");
				}
			} else if (b.getType() == 3) {
				b.setData(caluclate(aValue, new BigDecimal(b.getData().toString()), op, "除法里分母为0,请检查加工方式"));
				return b;
			} else if (b.getType() == 2) {
				List<JSONObject> list = JSONObject.parseArray(b.getData().toString(), JSONObject.class);
				for (int i = 0; i < list.size(); i++) {
					list.get(i).put(b.getField(), caluclate(aValue, list.get(i).getBigDecimal(b.getField()), op,
							"除法里分母为0,具体分母数据：" + b.getField() + "(" + list.get(i) + ")"));
				}
				b.setData(list);
				return b;
			} else {
				JSONObject object = JSONObject.parseObject(b.getData().toString());
				object.put(b.getField(), caluclate(aValue, object.getBigDecimal(b.getField()), op,
						"除法里分母为0,具体分母数据：" + b.getField() + "(" + object + ")"));
				b.setData(object);
				return b;
			}

		}
		// 代表a查出来的数据是只有一个
		if (a.getType() == 1) {
			JSONObject aObject = JSONObject.parseObject(a.getData().toString());
			BigDecimal aValue = aObject.getBigDecimal(a.getField());
			if (b.getType() == 0) {
				if (op.equals("+") || op.equals("-")) {
					return a;
				} else if (op.equals("*")) {
					return b;
				} else {
					throw new UnsupportedOperationException("除法里分母为0,具体分母数据：" + b.getField() + "(" + b.getData() + ")");
				}
			} else if (b.getType() == 3) {
				aObject.put(a.getField(),
						caluclate(aValue, new BigDecimal(b.getData().toString()), op, "除法里分母为0,请检查加工方式"));
				a.setData(aObject);
				return a;
			} else if (b.getType() == 1) {
				// 都有维度
				if (b.getDimensions() != null && a.getDimensions() != null) {
					// 如果维度不同，报错
					if (!SetUtils.isEqualSet(b.getDimensions(), a.getDimensions())) {
						throw new UnsupportedOperationException("维度不同的指标,不能做四则运算");
					} else {
						// 维度相同的运算 1对1
						JSONObject bObject = JSONObject.parseObject(b.getData().toString());
						BigDecimal bValue = bObject.getBigDecimal(b.getField());
						if (dimensionValueEqual(aObject, bObject, a.getDimensions())) {
							aObject.put(a.getField(), caluclate(aValue, bValue, op,
									"除法里分母为0,具体分母数据：" + b.getField() + "(" + b.getData() + ")"));
							a.setData(aObject);
							return a;
						} else {
							if (op.equals("*") || op.equals("/")) {
								throw new UnsupportedOperationException("维度的值不同的指标,不能做*/运算");
							} else {
								if (op.equals("-")) {
									// 将b的数变成相反数
									bValue = bValue.negate();
									bObject.put(b.getField(), bValue);
								}
								// 合并成list返回
								List<JSONObject> list = new ArrayList<JSONObject>();
								list.add(aObject);
								list.add(bObject);
								a.setData(list);
								return a;
							}
						}
					}

				} else {
					// 一个有维度，一个没有维度，或者都没维度 进行四则运算
					JSONObject bObject = JSONObject.parseObject(b.getData().toString());
					BigDecimal bValue = bObject.getBigDecimal(b.getField());
					aObject.put(a.getField(),
							caluclate(aValue, bValue, op, "除法里分母为0,具体分母数据：" + b.getField() + "(" + b.getData() + ")"));
					a.setData(aObject);
					if (b.getDimensions() != null && b.getDimensions().size() > 0) {
						a.setDimensions(b.getDimensions());
						a.setDimensionIds(b.getDimensionIds());
					}
					return a;
				}
			} else { // b为多维的
				List<JSONObject> list = JSONObject.parseArray(b.getData().toString(), JSONObject.class);
				if (a.getDimensions() == null || a.getDimensions().size() == 0) {
					for (int i = 0; i < list.size(); i++) {
						list.get(i).put(b.getField(), caluclate(aValue, list.get(i).getBigDecimal(b.getField()), op,
								"除法里分母为0,具体分母数据：" + b.getField() + "(" + list.get(i) + ")"));
					}
					b.setData(list);
					return b;
				} else {
					if (b.getDimensions() == null || b.getDimensions().size() == 0) {
						throw new UnsupportedOperationException("有维度的数据与多条没有维度的数据不能做四则运算");
					} else {
						if (!SetUtils.isEqualSet(b.getDimensions(), a.getDimensions())) {
							throw new UnsupportedOperationException("维度不同的指标,不能做四则运算");
						} else {
							// 维度相同的运算 1对多
							if (op.equals("*") || op.equals("/")) {
								throw new UnsupportedOperationException("维度相同，数目不相同的指标不能做*/运算");
							}
							// 相减，将b变成负数
							if (op.equals("-")) {
								for (int i = 0; i < list.size(); i++) {
									list.get(i).put(b.getField(), list.get(i).getBigDecimal(b.getField()).negate());
								}
							}
							return addN1(b, a);
						}
					}
				}
			}
		}

		// 代表查出来的数据是多个
		if (a.getType() == 2) {
			List<JSONObject> list = JSONObject.parseArray(a.getData().toString(), JSONObject.class);
			if (b.getType() == 0) {
				if (op.equals("+") || op.equals("-")) {
					return a;
				} else if (op.equals("*")) {
					return b;
				} else {
					throw new UnsupportedOperationException("除法里分母为0,具体分母数据：" + b.getField() + "(" + b.getData() + ")");
				}
			} else if (b.getType() == 3) {
				for (int i = 0; i < list.size(); i++) {
					BigDecimal aValue = list.get(i).getBigDecimal(a.getField());
					list.get(i).put(a.getField(),
							caluclate(aValue, new BigDecimal(b.getData().toString()), op, "除法里分母为0,请检查加工方式"));
				}
				a.setData(list);
				return a;
			} else if (b.getType() == 1) {
				// b为1个
				JSONObject bObject = JSONObject.parseObject(b.getData().toString());
				BigDecimal bValue = bObject.getBigDecimal(b.getField());
				if (b.getDimensions() == null || b.getDimensions().size() == 0) {
					for (int i = 0; i < list.size(); i++) {
						list.get(i).put(a.getField(), caluclate(list.get(i).getBigDecimal(a.getField()), bValue, op,
								"除法里分母为0,具体分母数据：" + bObject));
					}
					a.setData(list);
					return a;
				} else {
					if (a.getDimensions() == null || a.getDimensions().size() == 0) {
						throw new UnsupportedOperationException("有维度的数据与多条没有维度的数据不能做四则运算");
					} else {
						if (!SetUtils.isEqualSet(b.getDimensions(), a.getDimensions())) {
							throw new UnsupportedOperationException("维度不同的指标,不能做四则运算");
						} else {
							// 维度相同的运算 n对1
							if (op.equals("*") || op.equals("/")) {
								throw new UnsupportedOperationException("维度相同，数目不相同的指标不能做*/运算");
							}
							// 做+-操作 如果是-，将b变为相反数
							if (op.equals("-")) {
								bObject.put(b.getField(), bValue.negate());
							}
							return addN1(a, b);
						}
					}
				}
			} else {
				// b为多维数据
				if (a.getDimensions() == null || a.getDimensions().size() == 0 || b.getDimensions() == null
						|| b.getDimensions().size() == 0
						|| !SetUtils.isEqualSet(a.getDimensions(), b.getDimensions())) {
					throw new UnsupportedOperationException("两个多条的指标数据是不能做四则运算的");
				}
				List<JSONObject> bList = JSONObject.parseArray(b.getData().toString(), JSONObject.class);
				if (op.equals("*") || op.equals("/")) {
					if (list.size() != bList.size()) {
						throw new UnsupportedOperationException("都包含多个数据的指标,数据量不相等是不能做*/运算");
					}
				}
				for (int i = 0; i < list.size(); i++) {
					JSONObject aObject = list.get(i);
					for (int j = 0; j < bList.size(); j++) {
						JSONObject bObject = bList.get(j);
						if (dimensionValueEqual(aObject, bObject, a.getDimensions())) {
							aObject.put(a.getField(), caluclate(aObject.getBigDecimal(a.getField()),
									bObject.getBigDecimal(b.getField()), op, "除法里分母为0,具体分母数据：" + bObject));
							list.set(i, aObject);
							bList.remove(j);
							break;
						}
						if (j == bList.size() - 1) {
							if (op.equals("*") || op.equals("/")) {
								throw new UnsupportedOperationException("指标维度值不能一一对应时,是不能做*/运算");
							}
						}
					}
				}
				if (op.equals("*") || op.equals("/")) {
					a.setData(list);
					return a;
				} else {
					// 如果是符号-,减数变相反数
					if (op.equals("-")) {
						for (int i = 0; i < bList.size(); i++) {
							JSONObject bObject = bList.get(i);
							bObject.put(b.getField(), bObject.getBigDecimal(b.getField()).negate());
							bList.set(i, bObject);
						}
					}
					list.addAll(bList);
					a.setData(list);
					return a;
				}

			}
		}

		DataCommonResponse calculateValue = new DataCommonResponse();
		return calculateValue;
	}

	public boolean dimensionValueEqual(JSONObject a, JSONObject b, Set<String> dimensions) {
		for (String dimension : dimensions) {
			if (!a.getString(dimension).equals(b.getString(dimension))) {
				return false;
			}
		}
		return true;
	}

	// n+1 都有维度的数据相加
	public DataCommonResponse addN1(DataCommonResponse a, DataCommonResponse b) {
		List<JSONObject> list = JSONObject.parseArray(a.getData().toString(), JSONObject.class);
		JSONObject bObject = JSONObject.parseObject(b.getData().toString());
		for (int i = 0; i < list.size(); i++) {
			JSONObject aObject = list.get(i);
			if (dimensionValueEqual(aObject, bObject, a.getDimensions())) {
				aObject.put(a.getField(), aObject.getBigDecimal(a.getField()).add(bObject.getBigDecimal(b.getField())));
				list.set(i, aObject);
				a.setData(list);
				return a;
			}
		}
		list.add(bObject);
		a.setData(list);
		return a;
	}

	public BigDecimal caluclate(BigDecimal aValue, BigDecimal bvalue, String op, String exceptionMessage) {
		if (op.equals("+")) {
			return aValue.add(bvalue);
		} else if (op.equals("-")) {
			return aValue.subtract(bvalue);
		} else if (op.equals("*")) {
			return aValue.multiply(bvalue);
		} else {
			if (bvalue.floatValue() == 0) {
				// 抛异常，除数的分母为0
				throw new UnsupportedOperationException(exceptionMessage);
			}
			return aValue.divide(bvalue);
		}
	}

	public String getAdjectiveReq(Adjective adjective) {
		// 1为时间修饰词
		if (adjective.getType() == 1) {
			String result = "'" + adjective.getName();
			if (adjective.getCode_name().equals("last1HOUR")) {
				result = result + ">=':'" + DateTimeUtils.getlast1HOUR() + "'";
			} else if (adjective.getCode_name().equals("last1DAY")) {
				result = result + ">=':'" + DateTimeUtils.getLastDateByDay(0) + "'";
			} else if (adjective.getCode_name().equals("last2DAY")) {
				result = result + ">=':'" + DateTimeUtils.getLastDateByDay(-1) + "'";
			} else if (adjective.getCode_name().equals("last3DAY")) {
				result = result + ">=':'" + DateTimeUtils.getLastDateByDay(-2) + "'";
			} else if (adjective.getCode_name().equals("last7DAY") || adjective.getCode_name().equals("last1WEEK")) {
				result = result + ">=':'" + DateTimeUtils.getLastDateByDay(-6) + "'";
			} else if (adjective.getCode_name().equals("last14DAY")) {
				result = result + ">=':'" + DateTimeUtils.getLastDateByDay(-13) + "'";
			} else if (adjective.getCode_name().equals("last15DAY")) {
				result = result + ">=':'" + DateTimeUtils.getLastDateByDay(-15) + "'";
			} else if (adjective.getCode_name().equals("last30DAY")) {
				result = result + ">=':'" + DateTimeUtils.getLastDateByDay(-30) + "'";
			} else if (adjective.getCode_name().equals("last60DAY")) {
				result = result + ">=':'" + DateTimeUtils.getLastDateByDay(-60) + "'";
			} else if (adjective.getCode_name().equals("last90DAY")) {
				result = result + ">=':'" + DateTimeUtils.getLastDateByDay(-90) + "'";
			} else if (adjective.getCode_name().equals("last180DAY")) {
				result = result + ">=':'" + DateTimeUtils.getLastDateByDay(-180) + "'";
			} else if (adjective.getCode_name().equals("last360DAY")) {
				result = result + ">=':'" + DateTimeUtils.getLastDateByDay(-360) + "'";
			} else if (adjective.getCode_name().equals("last365DAY")) {
				result = result + ">=':'" + DateTimeUtils.getLastDateByDay(-365) + "'";
			} else if (adjective.getCode_name().equals("last1MONTH")) {
				result = result + ">=':'" + DateTimeUtils.getlastDateByMonth(-1) + "'";
			} else if (adjective.getCode_name().equals("last2MONTH")) {
				result = result + ">=':'" + DateTimeUtils.getlastDateByMonth(-2) + "'";
			} else if (adjective.getCode_name().equals("last3MONTH")) {
				result = result + ">=':'" + DateTimeUtils.getlastDateByMonth(-3) + "'";
			} else if (adjective.getCode_name().equals("last6MONTH")) {
				result = result + ">=':'" + DateTimeUtils.getlastDateByMonth(-6) + "'";
			} else if (adjective.getCode_name().equals("last7MONTH")) {
				result = result + ">=':'" + DateTimeUtils.getlastDateByMonth(-7) + "'";
			} else if (adjective.getCode_name().equals("last8MONTH")) {
				result = result + ">=':'" + DateTimeUtils.getlastDateByMonth(-8) + "'";
			} else if (adjective.getCode_name().equals("last1YEAR")) {
				result = result + ">=':'" + DateTimeUtils.getlastDateByYear(-1) + "'";
			} else if (adjective.getCode_name().equals("last2YEAR")) {
				result = result + ">=':'" + DateTimeUtils.getlastDateByYear(-2) + "'";
			} else if (adjective.getCode_name().equals("ftDate(w)")) {
				result = result + ">=':'" + DateTimeUtils.ftDateWeek() + "'";
			} else if (adjective.getCode_name().equals("ftDate(m)")) {
				result = result + ">=':'" + DateTimeUtils.ftDateMonth() + "'";
			} else if (adjective.getCode_name().equals("ftDate(q)")) {
				result = result + ">=':'" + DateTimeUtils.ftDateQuarter() + "'";
			} else if (adjective.getCode_name().equals("ftDate(y)")) {
				result = result + ">=':'" + DateTimeUtils.ftDateYear() + "'";
			} else if (adjective.getCode_name().equals("pre1MONTH")) {
				result = result + "&{}':'>=\\'" + DateTimeUtils.getPreDateByMonth(-1) + "\\',<\\'"
						+ DateTimeUtils.getPreDateByMonth(0) + "\\''";
			} else if (adjective.getCode_name().equals("pre2MONTH")) {
				result = result + "&{}':'>=\\'" + DateTimeUtils.getPreDateByMonth(-2) + "\\',<\\'"
						+ DateTimeUtils.getPreDateByMonth(0) + "\\''";
			} else if (adjective.getCode_name().equals("pre3MONTH")) {
				result = result + "&{}':'>=\\'" + DateTimeUtils.getPreDateByMonth(-3) + "\\',<\\'"
						+ DateTimeUtils.getPreDateByMonth(0) + "\\''";
			} else if (adjective.getCode_name().equals("pre4MONTH")) {
				result = result + "&{}':'>=\\'" + DateTimeUtils.getPreDateByMonth(-4) + "\\',<\\'"
						+ DateTimeUtils.getPreDateByMonth(0) + "\\''";
			} else if (adjective.getCode_name().equals("pre5MONTH")) {
				result = result + "&{}':'>=\\'" + DateTimeUtils.getPreDateByMonth(-5) + "\\',<\\'"
						+ DateTimeUtils.getPreDateByMonth(0) + "\\''";
			} else if (adjective.getCode_name().equals("pre6MONTH")) {
				result = result + "&{}':'>=\\'" + DateTimeUtils.getPreDateByMonth(-6) + "\\',<\\'"
						+ DateTimeUtils.getPreDateByMonth(0) + "\\''";
			} else if (adjective.getCode_name().equals("pre12MONTH")) {
				result = result + "&{}':'>=\\'" + DateTimeUtils.getPreDateByMonth(-12) + "\\',<\\'"
						+ DateTimeUtils.getPreDateByMonth(0) + "\\''";
			} else if (adjective.getCode_name().equals("pre24MONTH")) {
				result = result + "&{}':'>=\\'" + DateTimeUtils.getPreDateByMonth(-24) + "\\',<\\'"
						+ DateTimeUtils.getPreDateByMonth(0) + "\\''";
			} else if (adjective.getCode_name().equals("pre1DAY")) {
				result = result + "&{}':'>=\\'" + DateTimeUtils.getLastDateByDay(-1) + "\\',<\\'"
						+ DateTimeUtils.getLastDateByDay(0) + "\\''";
			} else if (adjective.getCode_name().equals("pre2DAY")) {
				result = result + "&{}':'>=\\'" + DateTimeUtils.getLastDateByDay(-2) + "\\',<\\'"
						+ DateTimeUtils.getLastDateByDay(0) + "\\''";
			} else if (adjective.getCode_name().equals("pre3DAY")) {
				result = result + "&{}':'>=\\'" + DateTimeUtils.getLastDateByDay(-3) + "\\',<\\'"
						+ DateTimeUtils.getLastDateByDay(0) + "\\''";
			} else if (adjective.getCode_name().equals("pre7DAY")) {
				result = result + "&{}':'>=\\'" + DateTimeUtils.getLastDateByDay(-7) + "\\',<\\'"
						+ DateTimeUtils.getLastDateByDay(0) + "\\''";
			} else if (adjective.getCode_name().equals("pre14DAY")) {
				result = result + "&{}':'>=\\'" + DateTimeUtils.getLastDateByDay(-14) + "\\',<\\'"
						+ DateTimeUtils.getLastDateByDay(0) + "\\''";
			} else if (adjective.getCode_name().equals("pre15DAY")) {
				result = result + "&{}':'>=\\'" + DateTimeUtils.getLastDateByDay(-15) + "\\',<\\'"
						+ DateTimeUtils.getLastDateByDay(0) + "\\''";
			} else if (adjective.getCode_name().equals("pre30DAY")) {
				result = result + "&{}':'>=\\'" + DateTimeUtils.getLastDateByDay(-30) + "\\',<\\'"
						+ DateTimeUtils.getLastDateByDay(0) + "\\''";
			} else if (adjective.getCode_name().equals("pre60DAY")) {
				result = result + "&{}':'>=\\'" + DateTimeUtils.getLastDateByDay(-60) + "\\',<\\'"
						+ DateTimeUtils.getLastDateByDay(0) + "\\''";
			} else if (adjective.getCode_name().equals("pre90DAY")) {
				result = result + "&{}':'>=\\'" + DateTimeUtils.getLastDateByDay(-90) + "\\',<\\'"
						+ DateTimeUtils.getLastDateByDay(0) + "\\''";
			} else if (adjective.getCode_name().equals("pre180DAY")) {
				result = result + "&{}':'>=\\'" + DateTimeUtils.getLastDateByDay(-180) + "\\',<\\'"
						+ DateTimeUtils.getLastDateByDay(0) + "\\''";
			} else if (adjective.getCode_name().equals("pre360DAY")) {
				result = result + "&{}':'>=\\'" + DateTimeUtils.getLastDateByDay(-360) + "\\',<\\'"
						+ DateTimeUtils.getLastDateByDay(0) + "\\''";
			} else if (adjective.getCode_name().equals("pre365DAY")) {
				result = result + "&{}':'>=\\'" + DateTimeUtils.getLastDateByDay(-365) + "\\',<\\'"
						+ DateTimeUtils.getLastDateByDay(0) + "\\''";
			} else if (adjective.getCode_name().equals("pre1YEAR")) {
				result = result + "&{}':'>=\\'" + DateTimeUtils.getPreDateByYear(-1) + "\\',<\\'"
						+ DateTimeUtils.getPreDateByYear(0) + "\\''";
			} else if (adjective.getCode_name().equals("pre1QUARTER")) {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				Calendar cal = Calendar.getInstance();
				int currentMonth = cal.get(Calendar.MONTH) + 1;
				if (currentMonth >= 1 && currentMonth <= 3) {
					cal.set(Calendar.MONTH, 0);
				} else if (currentMonth >= 4 && currentMonth <= 6) {
					cal.set(Calendar.MONTH, 3);
				} else if (currentMonth >= 7 && currentMonth <= 9) {
					cal.set(Calendar.MONTH, 4);
				} else if (currentMonth >= 10 && currentMonth <= 12) {
					cal.set(Calendar.MONTH, 9);
				}
				cal.set(Calendar.DATE, 1);
				String end = format.format(cal.getTime()) + " 00:00:00";
				cal.add(Calendar.MONTH, -3);
				String start = format.format(cal.getTime()) + " 00:00:00";
				result = result + "&{}':'>=\\'" + start + "\\',<\\'" + end + "\\''";
			}
			return result;
		}

		// 其它修饰词
		return "'" + adjective.getName() + adjective.getCode().substring(1);
	}

	public DataCommonResponse caculate(String str, int page, int count) {
		Stack<Character> priStack = new Stack<Character>();// 操作符栈
		Stack<DataCommonResponse> numStack = new Stack<DataCommonResponse>();
		;// 操作数栈
			// 1.判断string当中有没有非法字符
		String temp;// 用来临时存放读取的字符
		// 2.循环开始解析字符串，当字符串解析完，且符号栈为空时，则计算完成
		StringBuffer tempNum = new StringBuffer();// 用来临时存放数字字符串(当为多位数时)
		StringBuffer string = new StringBuffer().append(str);// 用来保存，提高效率

		while (string.length() != 0) {
			temp = string.substring(0, 1);
			string.delete(0, 1);
			// 判断temp，当temp为操作符时
			if (isOperator(temp)) {
				// 1.此时的tempNum内即为需要操作的数，取出数，压栈，并且清空tempNum
				if (!"".equals(tempNum.toString())) {
					// 当表达式的第一个符号为括号
					String num = tempNum.toString();
					numStack.push(getCalculateValue(num, page, count));
					tempNum.delete(0, tempNum.length());
				}
				// 用当前取得的运算符与栈顶运算符比较优先级：若高于，则因为会先运算，放入栈顶；若等于，因为出现在后面，所以会后计算，所以栈顶元素出栈，取出操作数运算；
				// 若小于，则同理，取出栈顶元素运算，将结果入操作数栈。

				// 判断当前运算符与栈顶元素优先级，取出元素，进行计算(因为优先级可能小于栈顶元素，还小于第二个元素等等，需要用循环判断)
				while (!compare(temp.charAt(0), priStack) && (!priStack.empty())) {
					DataCommonResponse a = numStack.pop();// 第二个运算数
					DataCommonResponse b = numStack.pop();// 第一个运算数
					char ope = priStack.pop();
					DataCommonResponse result = null;// 运算结果
					switch (ope) {
					// 如果是加号或者减号，则
					case '+':
						result = CalculateValue(b, a, "+");
						// 将操作结果放入操作数栈
						numStack.push(result);
						break;
					case '-':
						result = CalculateValue(b, a, "-");
						// 将操作结果放入操作数栈
						numStack.push(result);
						break;
					case '*':
						result = CalculateValue(b, a, "*");
						// 将操作结果放入操作数栈
						numStack.push(result);
						break;
					case '/':
						result = CalculateValue(b, a, "/");
						numStack.push(result);
						break;
					}

				}
				// 判断当前运算符与栈顶元素优先级， 如果高，或者低于平，计算完后，将当前操作符号，放入操作符栈
				if (temp.charAt(0) != '#') {
					priStack.push(new Character(temp.charAt(0)));
					if (temp.charAt(0) == ')') {// 当栈顶为'('，而当前元素为')'时，则是括号内以算完，去掉括号
						priStack.pop();
						priStack.pop();
					}
				}
			} else
				// 当为非操作符时（数字）
				tempNum = tempNum.append(temp);// 将读到的这一位数接到以读出的数后(当不是个位数的时候)
		}
		return numStack.pop();
	}

	/**
	 * 判断传入的字符是不是0-9的数字
	 * 
	 * @param str 传入的字符串
	 * @return
	 */
	private boolean isOperator(String temp) {
		boolean isOperator = temp.equals("(") || temp.equals(")") || temp.equals("+") || temp.equals("-")
				|| temp.equals("*") || temp.equals("/") || temp.equals("#");
		return isOperator;
	}

	/**
	 * 比较当前操作符与栈顶元素操作符优先级，如果比栈顶元素优先级高，则返回true，否则返回false
	 * 
	 * @param str 需要进行比较的字符
	 * @return 比较结果 true代表比栈顶元素优先级高，false代表比栈顶元素优先级低
	 */
	private boolean compare(char str, Stack<Character> priStack) {
		if (priStack.empty()) {
			// 当为空时，显然 当前优先级最低，返回高
			return true;
		}
		char last = (char) priStack.lastElement();
		// 如果栈顶为'('显然，优先级最低，')'不可能为栈顶。
		if (last == '(') {
			return true;
		}
		switch (str) {
		case '#':
			return false;// 结束符
		case '(':
			// '('优先级最高,显然返回true
			return true;
		case ')':
			// ')'优先级最低，
			return false;
		case '*': {
			// '*/'优先级只比'+-'高
			if (last == '+' || last == '-')
				return true;
			else
				return false;
		}
		case '/': {
			if (last == '+' || last == '-')
				return true;
			else
				return false;
		}
		// '+-'为最低，一直返回false
		case '+':
			return false;
		case '-':
			return false;
		}
		return true;
	}

}
