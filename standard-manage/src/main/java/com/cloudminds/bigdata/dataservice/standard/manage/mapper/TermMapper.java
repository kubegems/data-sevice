package com.cloudminds.bigdata.dataservice.standard.manage.mapper;

import java.util.List;

import com.cloudminds.bigdata.dataservice.standard.manage.entity.response.TermExtendInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.cloudminds.bigdata.dataservice.standard.manage.entity.Term;

@Mapper
public interface TermMapper {
	@Insert("insert into term(zh_name, classify_id,en_name,term_field,create_time,update_time, creator,descr) values(#{zh_name},#{classify_id}, #{en_name}, #{term_field}, now(),now(), #{creator}, #{descr})")
	public int insertTerm(Term term) throws Exception;
	
	@Update("update term set zh_name=#{zh_name}, classify_id=#{classify_id},en_name=#{en_name},term_field=#{term_field},descr=#{descr} where id=#{id}")
	public int updateTerm(Term term) throws Exception;

	@Select("select * from term where zh_name=#{zh} and deleted=0")
	public Term findTermByZh(String zh);

	@Select("select * from term where en_name=#{en} and deleted=0")
	public Term findTermByEn(String en);

	@Select("select * from term where id=#{id} and deleted=0")
	public Term findTermById(int id);

	@Select("select * from term where term_field=#{field} and deleted=0")
	public Term findTermByField(String field);

	@Update("update term set deleted=1 where id=#{id}")
	public int deleteTermById(int id);

	@Update({"<script> update term set deleted=1 where id in <foreach collection='array' item='id' index='no' open='(' separator=',' close=')'> #{id} </foreach></script>"})
	public int batchDeleteTerm(int[] id);
	
	@Select("select t.*,three.id as classify_id,three.name as classify_name from term t left join classify three on t.classify_id=three.id where t.deleted=0 ${condition} order by t.update_time desc limit #{startLine},#{size}")
	public List<TermExtendInfo> queryTerm(String condition, int startLine, int size);
	
	@Select("select count(*) from term t left join classify three on t.classify_id=three.id where t.deleted=0 ${condition}")
	public int queryTermCount(String condition);

}
