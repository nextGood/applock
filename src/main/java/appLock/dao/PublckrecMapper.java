package appLock.dao;

import org.springframework.stereotype.Repository;

import appLock.model.Publckrec;

@Repository
public interface PublckrecMapper {
	int deleteByPrimaryKey(String reckey);

	int insert(Publckrec record);

	int insertSelective(Publckrec record);

	Publckrec selectByPrimaryKey(String reckey);

	int updateByPrimaryKeySelective(Publckrec record);

	int updateByPrimaryKey(Publckrec record);
}