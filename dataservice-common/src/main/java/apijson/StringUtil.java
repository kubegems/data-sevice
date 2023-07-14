/*Copyright (C) 2020 THL A29 Limited, a Tencent company.  All rights reserved.

This source code is licensed under the Apache License Version 2.0.*/

package apijson;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import apijson.entity.MethodReplaceResult;
import apijson.orm.AbstractSQLConfig;

/**
 * 通用字符串(String)相关类,为null时返回""
 * 
 * @author Lemon
 * @use StringUtil.
 */
public class StringUtil {
	private static final String TAG = "StringUtil";

	public StringUtil() {
	}

	public static final String UTF_8 = "utf-8";

	public static final String EMPTY = "无";
	public static final String UNKNOWN = "未知";
	public static final String UNLIMITED = "不限";

	public static final String I = "我";
	public static final String YOU = "你";
	public static final String HE = "他";
	public static final String SHE = "她";
	public static final String IT = "它";

	public static final String MALE = "男";
	public static final String FEMALE = "女";

	public static final String TODO = "未完成";
	public static final String DONE = "已完成";

	public static final String FAIL = "失败";
	public static final String SUCCESS = "成功";

	public static final String SUNDAY = "日";
	public static final String MONDAY = "一";
	public static final String TUESDAY = "二";
	public static final String WEDNESDAY = "三";
	public static final String THURSDAY = "四";
	public static final String FRIDAY = "五";
	public static final String SATURDAY = "六";

	public static final String YUAN = "元";

	private static String currentString = "";

	/**
	 * 获取刚传入处理后的string
	 * 
	 * @must 上个影响currentString的方法 和 这个方法都应该在同一线程中，否则返回值可能不对
	 * @return
	 */
	public static String getCurrentString() {
		return currentString == null ? "" : currentString;
	}

	// 获取string,为null时返回"" <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	/**
	 * 获取string,为null则返回""
	 * 
	 * @param object
	 * @return
	 */
	public static String getString(Object object) {
		return object == null ? "" : object.toString();
	}

	/**
	 * 获取string,为null则返回""
	 * 
	 * @param cs
	 * @return
	 */
	public static String getString(CharSequence cs) {
		return cs == null ? "" : cs.toString();
	}

	/**
	 * 获取string,为null则返回""
	 * 
	 * @param s
	 * @return
	 */
	public static String getString(String s) {
		return s == null ? "" : s;
	}

	/**
	 * 获取string,为null则返回"" ignoreEmptyItem = false; split = ","
	 * 
	 * @param array
	 * @return {@link #getString(Object[], boolean)}
	 */
	public static String getString(Object[] array) {
		return getString(array, false);
	}

	/**
	 * 获取string,为null则返回"" split = ","
	 * 
	 * @param array
	 * @param ignoreEmptyItem
	 * @return {@link #getString(Object[], boolean)}
	 */
	public static String getString(Object[] array, boolean ignoreEmptyItem) {
		return getString(array, null, ignoreEmptyItem);
	}

	/**
	 * 获取string,为null则返回"" ignoreEmptyItem = false;
	 * 
	 * @param array
	 * @param split
	 * @return {@link #getString(Object[], String, boolean)}
	 */
	public static String getString(Object[] array, String split) {
		return getString(array, split, false);
	}

	/**
	 * 获取string,为null则返回""
	 * 
	 * @param array
	 * @param split
	 * @param ignoreEmptyItem
	 * @return
	 */
	public static String getString(Object[] array, String split, boolean ignoreEmptyItem) {
		String s = "";
		if (array != null) {
			if (split == null) {
				split = ",";
			}
			for (int i = 0; i < array.length; i++) {
				if (ignoreEmptyItem && isEmpty(array[i], true)) {
					continue;
				}
				s += ((i > 0 ? split : "") + array[i]);
			}
		}
		return getString(s);
	}

	// 获取string,为null时返回"" >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	// 获取去掉前后空格后的string<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	/**
	 * 获取去掉前后空格后的string,为null则返回""
	 * 
	 * @param object
	 * @return
	 */
	public static String getTrimedString(Object object) {
		return getTrimedString(getString(object));
	}

	/**
	 * 获取去掉前后空格后的string,为null则返回""
	 * 
	 * @param cs
	 * @return
	 */
	public static String getTrimedString(CharSequence cs) {
		return getTrimedString(getString(cs));
	}

	/**
	 * 获取去掉前后空格后的string,为null则返回""
	 * 
	 * @param s
	 * @return
	 */
	public static String getTrimedString(String s) {
		return getString(s).trim();
	}

	// 获取去掉前后空格后的string>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	// 获取去掉所有空格后的string <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	/**
	 * 获取去掉所有空格后的string,为null则返回""
	 * 
	 * @param object
	 * @return
	 */
	public static String getNoBlankString(Object object) {
		return getNoBlankString(getString(object));
	}

	/**
	 * 获取去掉所有空格后的string,为null则返回""
	 * 
	 * @param cs
	 * @return
	 */
	public static String getNoBlankString(CharSequence cs) {
		return getNoBlankString(getString(cs));
	}

	/**
	 * 获取去掉所有空格后的string,为null则返回""
	 * 
	 * @param s
	 * @return
	 */
	public static String getNoBlankString(String s) {
		return getString(s).replaceAll("\\s", "");
	}

	// 获取去掉所有空格后的string >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	// 获取string的长度<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	/**
	 * 获取string的长度,为null则返回0
	 * 
	 * @param object
	 * @param trim
	 * @return
	 */
	public static int getLength(Object object, boolean trim) {
		return getLength(getString(object), trim);
	}

	/**
	 * 获取string的长度,为null则返回0
	 * 
	 * @param cs
	 * @param trim
	 * @return
	 */
	public static int getLength(CharSequence cs, boolean trim) {
		return getLength(getString(cs), trim);
	}

	/**
	 * 获取string的长度,为null则返回0
	 * 
	 * @param s
	 * @param trim
	 * @return
	 */
	public static int getLength(String s, boolean trim) {
		s = trim ? getTrimedString(s) : s;
		return getString(s).length();
	}

	// 获取string的长度>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	// 判断字符是否为空 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	/**
	 * 判断字符是否为空
	 * 
	 * @param object
	 * @param trim
	 * @return
	 */
	public static boolean isEmpty(Object object, boolean trim) {
		return isEmpty(getString(object), trim);
	}

	/**
	 * 判断字符是否为空
	 * 
	 * @param cs
	 * @param trim
	 * @return
	 */
	public static boolean isEmpty(CharSequence cs, boolean trim) {
		return isEmpty(getString(cs), trim);
	}

	/**
	 * 判断字符是否为空
	 * 
	 * @param s
	 * @param trim
	 * @return
	 */
	public static boolean isEmpty(String s, boolean trim) {
		// Log.i(TAG, "getTrimedString s = " + s);
		if (s == null) {
			return true;
		}
		if (trim) {
			s = s.trim();
		}
		if (s.isEmpty()) {
			return true;
		}

		currentString = s;

		return false;
	}

	// 判断字符是否为空 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	// 判断字符是否非空 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	/**
	 * 判断字符是否非空
	 * 
	 * @param object
	 * @param trim
	 * @return
	 */
	public static boolean isNotEmpty(Object object, boolean trim) {
		return isNotEmpty(getString(object), trim);
	}

	/**
	 * 判断字符是否非空
	 * 
	 * @param cs
	 * @param trim
	 * @return
	 */
	public static boolean isNotEmpty(CharSequence cs, boolean trim) {
		return isNotEmpty(getString(cs), trim);
	}

	/**
	 * 判断字符是否非空
	 * 
	 * @param s
	 * @param trim
	 * @return
	 */
	public static boolean isNotEmpty(String s, boolean trim) {
		return !isEmpty(s, trim);
	}

	// 判断字符是否非空 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	// 判断字符类型 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	public static final Pattern PATTERN_NUMBER;
	public static final Pattern PATTERN_PHONE;
	public static final Pattern PATTERN_EMAIL;
	public static final Pattern PATTERN_ID_CARD;
	public static final Pattern PATTERN_ALPHA;
	public static final Pattern PATTERN_PASSWORD; // TODO
	public static final Pattern PATTERN_NAME;
	public static final Pattern PATTERN_ALPHA_BIG;
	public static final Pattern PATTERN_ALPHA_SMALL;
	// 第一个参数是函数名,第二个参数代表是否需要特殊处理true 需要 false 不需要
	public static final Map<String, Boolean> supportMethod = new HashMap<String, Boolean>();
	// 需要映射的函数名
	public static final Map<String, String> methodMap = new HashMap<String, String>();
	static {
		PATTERN_NUMBER = Pattern.compile("^[0-9]+$");
		PATTERN_ALPHA = Pattern.compile("^[a-zA-Z]+$");
		PATTERN_ALPHA_BIG = Pattern.compile("^[A-Z]+$");
		PATTERN_ALPHA_SMALL = Pattern.compile("^[a-z]+$");
		PATTERN_NAME = Pattern.compile("^[0-9a-zA-Z_]+$");// 已用55个中英字符测试通过
		PATTERN_PHONE = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0-2,5-9])|(17[0-9]))\\d{8}$");
		PATTERN_EMAIL = Pattern.compile(
				"^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
		PATTERN_ID_CARD = Pattern.compile(
				"(^[1-9]\\d{5}(18|19|([23]\\d))\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$)|(^[1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{2}$)");
		PATTERN_PASSWORD = Pattern.compile("^[0-9a-zA-Z]+$");
		supportMethod.put("year", false);
		supportMethod.put("month", false);
		supportMethod.put("dayOfMonth", false);
		supportMethod.put("quarter", false);
		supportMethod.put("dayOfWeek", false);
		supportMethod.put("hour", false);
		supportMethod.put("minute", false);
		supportMethod.put("second", false);
		supportMethod.put("toString",false);
		// 特殊处理
		supportMethod.put("date", true);
//		supportMethod.put("date_format", true);

		methodMap.put("CLICKHOUSE_year", "toYear");
		methodMap.put("CLICKHOUSE_month", "toMonth");
		methodMap.put("CLICKHOUSE_dayOfMonth", "toDayOfMonth");
		methodMap.put("CLICKHOUSE_quarter", "toQuarter");
		methodMap.put("CLICKHOUSE_dayOfWeek", "toDayOfWeek");
		methodMap.put("CLICKHOUSE_hour", "toHour");
		methodMap.put("CLICKHOUSE_minute", "toMinute");
		methodMap.put("CLICKHOUSE_second", "toSecond");
		methodMap.put("CLICKHOUSE_date", "toDate");

	}

//第二版sql处理
	public static MethodReplaceResult methodReplace(String sql, String database, String schema, String table,
			boolean isForce) {
		MethodReplaceResult methodReplaceResult = new MethodReplaceResult();
		methodReplaceResult.setSql(sql);
		if (database.equals("DATASERVICE")) {
			return methodReplaceResult;
		}
		int startLocation = sql.indexOf("(");
		if (startLocation == -1) {
			return methodReplaceResult;
		}
		if (sql.substring(startLocation + 1).contains("(")) {
			throw new IllegalArgumentException("此函数格式：" + sql + " 不合法！" + "不支持多层的函数！");
		}
		// 确定是函数了，提取函数名称
		int endLocation = sql.indexOf(")");
		if (endLocation == -1) {
			throw new IllegalArgumentException("此函数格式：" + sql + " 不合法！" + "函数没有闭合符号)！");
		}
		int methodStart = sql.substring(0, startLocation).lastIndexOf(" ") + 1;
		String methodName = sql.substring(methodStart, startLocation);
		// 函数不支持的情况
		if (supportMethod.get(methodName) == null) {
			if (isForce) {
				throw new IllegalArgumentException("不支持的函数：" + methodName);
			} else {
				return methodReplaceResult;
			}
		}

		// 函数支持且函数是不需要特殊处理的
		if (!supportMethod.get(methodName)) {
			String realMethodName = methodMap.get(database + "_" + methodName);
			if (realMethodName != null) {
				sql = new StringBuilder(sql).replace(methodStart, startLocation, realMethodName).toString();
				methodReplaceResult.setSql(sql);
			}
			return methodReplaceResult;
		}

		// 函数支持且函数需要特殊处理
		if (methodName.equals("date")) {
			if (database.equals("KYLIN")) {
				sql = new StringBuilder(sql).replace(methodStart, endLocation+1, "cast("
						+ columnReplace(database,"\"" + sql.substring(startLocation + 1, endLocation) + "\"", schema, table)
						+ " as date)").toString();
				methodReplaceResult.setColumnMap(true);
			} else {
				String realMethodName = methodMap.get(database + "_" + methodName);
				if (realMethodName != null) {
					sql = new StringBuilder(sql).replace(methodStart, startLocation, realMethodName).toString();
				}
			}
			methodReplaceResult.setSql(sql);
			return methodReplaceResult;
		}

		return null;

	}

	public static String columnReplace(String dbName,String column, String schema, String table) {
		if (AbstractSQLConfig.tableColumnMap.get(dbName+"."+ schema + "." + table + "_aliasColumn") != null
				&& AbstractSQLConfig.tableColumnMap.get(dbName+"."+schema + "." + table + "_aliasColumn").containsKey(column)) {
			column = AbstractSQLConfig.tableColumnMap.get(dbName+"."+schema + "." + table + "_aliasColumn").get(column);
		}
		return column;
	}

	// 第一版替换真实函数，这个是在最终生成sql的时候做处理
//	public static String methodReplace(String sql, String database) {
//		System.out.println(sql);
//		if (database.equals("DATASERVICE"))
//			return sql;
//		if (sql == null || sql.equals(""))
//			return sql;
//		String[] sqlArr = sql.split(" ");
//		for (int j = 0; j < sqlArr.length; j++) {
//			String[] data = sqlArr[j].split(",");
//			for (int n = 0; n < data.length; n++) {
//				StringBuilder dataString = new StringBuilder(data[n]);
//				int i = 0;
//				while (i >= 0) {
//					int location = dataString.indexOf("(", i);
//					if (location == -1) {
//						break;
//					}
//					String method = dataString.substring(i, location);
//					String newMethod = StringUtil.methodMap.get(database + "_" + method);
//					if (newMethod != null) {
//						dataString.replace(i, location, newMethod);
//						i = i + newMethod.length();
//						i = dataString.indexOf("(", i);
//					} else {
//						i = location;
//					}
//					i++;
//				}
//				data[n] = dataString.toString();
//			}
//			sqlArr[j] = arrayToString(data, ",");
//		}
//		System.out.println(arrayToString(sqlArr, " "));
//		return arrayToString(sqlArr, " ");
//	}
//
//	public static String arrayToString(String[] array, String a) {
//		String result = "";
//		for (int i = 0; i < array.length; i++) {
//			if (i == 0) {
//				result = result + array[i];
//			} else {
//				result = result + a + array[i];
//			}
//		}
//		return result;
//	}

	/**
	 * 判断手机格式是否正确
	 * 
	 * @param phone
	 * @return
	 */
	public static boolean isPhone(String phone) {
		if (isNotEmpty(phone, true) == false) {
			return false;
		}

		currentString = phone;
		return PATTERN_PHONE.matcher(phone).matches();
	}

	/**
	 * 判断手机格式是否正确
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isPassword(String s) {
		return getLength(s, false) >= 6 && PATTERN_PASSWORD.matcher(s).matches();
	}

	/**
	 * 判断是否全是数字密码
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isNumberPassword(String s) {
		return getLength(s, false) == 6 && isNumer(s);
	}

	/**
	 * 判断email格式是否正确
	 * 
	 * @param email
	 * @return
	 */
	public static boolean isEmail(String email) {
		if (isNotEmpty(email, true) == false) {
			return false;
		}

		currentString = email;
		return PATTERN_EMAIL.matcher(email).matches();
	}

	/**
	 * 判断是否全是验证码
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isVerify(String s) {
		return getLength(s, false) >= 4 && isNumer(s);
	}

	/**
	 * 判断是否全是数字
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isNumer(String s) {
		if (isNotEmpty(s, true) == false) {
			return false;
		}

		currentString = s;
		return PATTERN_NUMBER.matcher(s).matches();
	}

	/**
	 * 判断是否全是字母
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isAlpha(String s) {
		if (isEmpty(s, true)) {
			return false;
		}

		currentString = s;
		return PATTERN_ALPHA.matcher(s).matches();
	}

	/**
	 * 判断是否全是数字或字母
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isNumberOrAlpha(String s) {
		return isNumer(s) || isAlpha(s);
	}

	/**
	 * 判断是否为代码名称，只能包含字母，数字或下划线
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isName(String s) {
		return s != null && PATTERN_NAME.matcher(s).matches();
	}

	/**
	 * 判断是否为首字母大写的代码名称
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isBigName(String s) {
		s = getString(s);
		if (s.isEmpty() || PATTERN_ALPHA.matcher(s.substring(0, 1)).matches() == false) {
			return false;
		}
		return s.length() <= 1 ? true : isName(s.substring(1));
	}

	/**
	 * 判断是否为首字母小写的代码名称
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isSmallName(String s) {
		s = getString(s);
		if (s.isEmpty() || PATTERN_ALPHA_SMALL.matcher(s.substring(0, 1)).matches() == false) {
			return false;
		}
		return s.length() <= 1 ? true : isName(s.substring(1));
	}

	/**
	 * 判断字符类型是否是身份证号
	 * 
	 * @param number
	 * @return
	 */
	public static boolean isIDCard(String number) {
		if (isNumberOrAlpha(number) == false) {
			return false;
		}
		number = getString(number);
		if (number.length() == 15) {
			Log.i(TAG, "isIDCard number.length() == 15 old IDCard");
			currentString = number;
			return true;
		}
		if (number.length() == 18) {
			currentString = number;
			return true;
		}

		return false;
	}

	public static final String HTTP = "http";
	public static final String URL_PREFIX = "http://";
	public static final String URL_PREFIXs = "https://";
	public static final String URL_STAFFIX = URL_PREFIX;
	public static final String URL_STAFFIXs = URL_PREFIXs;

	/**
	 * 判断字符类型是否是网址
	 * 
	 * @param url
	 * @return
	 */
	public static boolean isUrl(String url) {
		if (isNotEmpty(url, true) == false) {
			return false;
		} else if (!url.startsWith(URL_PREFIX) && !url.startsWith(URL_PREFIXs)) {
			return false;
		}

		currentString = url;
		return true;
	}

	public static final String FILE_PATH_PREFIX = "file://";

	/**
	 * 判断文件路径是否存在
	 * 
	 * @param path
	 * @return
	 */
	public static boolean isFilePathExist(String path) {
		return StringUtil.isFilePath(path) && new File(path).exists();
	}

	public static final String SEPARATOR = "/";

	/**
	 * 判断是否为路径
	 * 
	 * @param path
	 * @return
	 */
	public static boolean isPath(String path) {
		return StringUtil.isNotEmpty(path, true) && path.contains(SEPARATOR)
				&& path.contains(SEPARATOR + SEPARATOR) == false && path.endsWith(SEPARATOR) == false;
	}

	/**
	 * 判断字符类型是否是路径
	 * 
	 * @param path
	 * @return
	 */
	public static boolean isFilePath(String path) {
		if (isNotEmpty(path, true) == false) {
			return false;
		}

		if (!path.contains(".") || path.endsWith(".")) {
			return false;
		}

		currentString = path;

		return true;
	}

	// 判断字符类型 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	// 提取特殊字符<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	/**
	 * 去掉string内所有非数字类型字符
	 * 
	 * @param object
	 * @return
	 */
	public static String getNumber(Object object) {
		return getNumber(getString(object));
	}

	/**
	 * 去掉string内所有非数字类型字符
	 * 
	 * @param cs
	 * @return
	 */
	public static String getNumber(CharSequence cs) {
		return getNumber(getString(cs));
	}

	/**
	 * 去掉string内所有非数字类型字符
	 * 
	 * @param s
	 * @return
	 */
	public static String getNumber(String s) {
		return getNumber(s, false);
	}

	/**
	 * 去掉string内所有非数字类型字符
	 * 
	 * @param s
	 * @param onlyStart 中间有非数字时只获取前面的数字
	 * @return
	 */
	public static String getNumber(String s, boolean onlyStart) {
		if (isNotEmpty(s, true) == false) {
			return "";
		}

		String numberString = "";
		String single;
		for (int i = 0; i < s.length(); i++) {
			single = s.substring(i, i + 1);
			if (isNumer(single)) {
				numberString += single;
			} else {
				if (onlyStart) {
					return numberString;
				}
			}
		}

		return numberString;
	}

	// 提取特殊字符>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	// 校正（自动补全等）字符串<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	/**
	 * 获取网址，自动补全
	 * 
	 * @param url
	 * @return
	 */
	public static String getCorrectUrl(String url) {
		Log.i(TAG, "getCorrectUrl : \n" + url);
		if (isNotEmpty(url, true) == false) {
			return "";
		}

		// if (! url.endsWith("/") && ! url.endsWith(".html")) {
		// url = url + "/";
		// }

		if (isUrl(url) == false) {
			return URL_PREFIX + url;
		}
		return url;
	}

	/**
	 * 获取去掉所有 空格 、"-" 、"+86" 后的phone
	 * 
	 * @param phone
	 * @return
	 */
	public static String getCorrectPhone(String phone) {
		if (isNotEmpty(phone, true) == false) {
			return "";
		}

		phone = getNoBlankString(phone);
		phone = phone.replaceAll("-", "");
		if (phone.startsWith("+86")) {
			phone = phone.substring(3);
		}
		return phone;
	}

	/**
	 * 获取邮箱，自动补全
	 * 
	 * @param email
	 * @return
	 */
	public static String getCorrectEmail(String email) {
		if (isNotEmpty(email, true) == false) {
			return "";
		}

		email = getNoBlankString(email);
		if (isEmail(email) == false && !email.endsWith(".com")) {
			email += ".com";
		}

		return email;
	}

	public static final int PRICE_FORMAT_DEFAULT = 0;
	public static final int PRICE_FORMAT_PREFIX = 1;
	public static final int PRICE_FORMAT_SUFFIX = 2;
	public static final int PRICE_FORMAT_PREFIX_WITH_BLANK = 3;
	public static final int PRICE_FORMAT_SUFFIX_WITH_BLANK = 4;
	public static final String[] PRICE_FORMATS = { "", "￥", "元", "￥ ", " 元" };

	/**
	 * 获取价格，保留两位小数
	 * 
	 * @param price
	 * @return
	 */
	public static String getPrice(String price) {
		return getPrice(price, PRICE_FORMAT_DEFAULT);
	}

	/**
	 * 获取价格，保留两位小数
	 * 
	 * @param price
	 * @param formatType 添加单位（元）
	 * @return
	 */
	public static String getPrice(String price, int formatType) {
		if (isNotEmpty(price, true) == false) {
			return getPrice(0, formatType);
		}

		// 单独写到getCorrectPrice? <<<<<<<<<<<<<<<<<<<<<<
		String correctPrice = "";
		String s;
		for (int i = 0; i < price.length(); i++) {
			s = price.substring(i, i + 1);
			if (".".equals(s) || isNumer(s)) {
				correctPrice += s;
			}
		}
		// 单独写到getCorrectPrice? >>>>>>>>>>>>>>>>>>>>>>

		Log.i(TAG, "getPrice  <<<<<<<<<<<<<<<<<< correctPrice =  " + correctPrice);
		if (correctPrice.contains(".")) {
			// if (correctPrice.startsWith(".")) {
			// correctPrice = 0 + correctPrice;
			// }
			if (correctPrice.endsWith(".")) {
				correctPrice = correctPrice.replaceAll(".", "");
			}
		}

		Log.i(TAG, "getPrice correctPrice =  " + correctPrice + " >>>>>>>>>>>>>>>>");
		return isNotEmpty(correctPrice, true) ? getPrice(new BigDecimal(0 + correctPrice), formatType)
				: getPrice(0, formatType);
	}

	/**
	 * 获取价格，保留两位小数
	 * 
	 * @param price
	 * @return
	 */
	public static String getPrice(BigDecimal price) {
		return getPrice(price, PRICE_FORMAT_DEFAULT);
	}

	/**
	 * 获取价格，保留两位小数
	 * 
	 * @param price
	 * @return
	 */
	public static String getPrice(double price) {
		return getPrice(price, PRICE_FORMAT_DEFAULT);
	}

	/**
	 * 获取价格，保留两位小数
	 * 
	 * @param price
	 * @param formatType 添加单位（元）
	 * @return
	 */
	public static String getPrice(BigDecimal price, int formatType) {
		return getPrice(price == null ? 0 : price.doubleValue(), formatType);
	}

	/**
	 * 获取价格，保留两位小数
	 * 
	 * @param price
	 * @param formatType 添加单位（元）
	 * @return
	 */
	public static String getPrice(double price, int formatType) {
		String s = new DecimalFormat("#########0.00").format(price);
		switch (formatType) {
		case PRICE_FORMAT_PREFIX:
			return PRICE_FORMATS[PRICE_FORMAT_PREFIX] + s;
		case PRICE_FORMAT_SUFFIX:
			return s + PRICE_FORMATS[PRICE_FORMAT_SUFFIX];
		case PRICE_FORMAT_PREFIX_WITH_BLANK:
			return PRICE_FORMATS[PRICE_FORMAT_PREFIX_WITH_BLANK] + s;
		case PRICE_FORMAT_SUFFIX_WITH_BLANK:
			return s + PRICE_FORMATS[PRICE_FORMAT_SUFFIX_WITH_BLANK];
		default:
			return s;
		}
	}

	/**
	 * 分割路径
	 * 
	 * @param path
	 * @return
	 */
	public static String[] splitPath(String path) {
		if (StringUtil.isNotEmpty(path, true) == false) {
			return null;
		}
		return isPath(path) ? split(path, SEPARATOR) : new String[] { path };
	}

	/**
	 * 将s分割成String[]
	 * 
	 * @param s
	 * @return
	 */
	public static String[] split(String s) {
		return split(s, null);
	}

	/**
	 * 将s用split分割成String[] trim = true;
	 * 
	 * @param s
	 * @param split
	 * @return
	 */
	public static String[] split(String s, String split) {
		return split(s, split, true);
	}

	/**
	 * 将s用split分割成String[]
	 * 
	 * @param s
	 * @param trim 去掉前后两端的split
	 * @return
	 */
	public static String[] split(String s, boolean trim) {
		return split(s, null, trim);
	}

	/**
	 * 将s用split分割成String[]
	 * 
	 * @param s
	 * @param split
	 * @param trim  去掉前后两端的split
	 * @return
	 */
	public static String[] split(String s, String split, boolean trim) {
		s = getString(s);
		if (s.isEmpty()) {
			return null;
		}
		if (isEmpty(split, false)) {
			split = ",";
		}
		if (trim) {
			while (s.startsWith(split)) {
				s = s.substring(split.length());
			}
			while (s.endsWith(split)) {
				s = s.substring(0, s.length() - split.length());
			}
		}
		return s.contains(split) ? s.split(split) : new String[] { s };
	}

	/**
	 * @param key
	 * @param suffix
	 * @return key + suffix，第一个字母小写
	 */
	public static String addSuffix(String key, String suffix) {
		key = getNoBlankString(key);
		if (key.isEmpty()) {
			return firstCase(suffix);
		}
		return firstCase(key) + firstCase(suffix, true);
	}

	/**
	 * @param key
	 */
	public static String firstCase(String key) {
		return firstCase(key, false);
	}

	/**
	 * @param key
	 * @param upper
	 * @return
	 */
	public static String firstCase(String key, boolean upper) {
		key = getString(key);
		if (key.isEmpty()) {
			return "";
		}

		String first = key.substring(0, 1);
		key = (upper ? first.toUpperCase() : first.toLowerCase()) + key.substring(1, key.length());

		return key;
	}

	/**
	 * 全部大写
	 * 
	 * @param s
	 * @return
	 */
	public static String toUpperCase(String s) {
		return toUpperCase(s, false);
	}

	/**
	 * 全部大写
	 * 
	 * @param s
	 * @param trim
	 * @return
	 */
	public static String toUpperCase(String s, boolean trim) {
		s = trim ? getTrimedString(s) : getString(s);
		return s.toUpperCase();
	}

	/**
	 * 全部小写
	 * 
	 * @param s
	 * @return
	 */
	public static String toLowerCase(String s) {
		return toLowerCase(s, false);
	}

	/**
	 * 全部小写
	 * 
	 * @param s
	 * @return
	 */
	public static String toLowerCase(String s, boolean trim) {
		s = trim ? getTrimedString(s) : getString(s);
		return s.toLowerCase();
	}

	public static String concat(String left, String right) {
		return concat(left, right, null);
	}

	public static String concat(String left, String right, String split) {
		return concat(left, right, split, true);
	}

	public static String concat(String left, String right, boolean trim) {
		return concat(left, right, null, trim);
	}

	public static String concat(String left, String right, String split, boolean trim) {
		if (isEmpty(left, trim)) {
			return right;
		}
		if (isEmpty(right, trim)) {
			return left;
		}

		if (split == null) {
			split = ",";
		}
		return left + split + right;
	}

	// 校正（自动补全等）字符串>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
}
