package appLock.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.github.pagehelper.Page;

import appLock.model.Publckrec;

@Service("AppLockService")
public class AppLockService {
	// TODO 修改日志配置文件
	private static final Logger localLogger = LoggerFactory.getLogger("local_log");

	/**
	 * 尝试加锁
	 * 
	 * @param param
	 * @return
	 */
	public Integer tryLock(String param) {
		param = param.trim();
		// 根据RecKey获取LckTim, TimOut
		PublckrecExample publckrecExample = new PublckrecExample();
		PublckrecExample.Criteria criteria = publckrecExample.createCriteria();
		criteria.andReckeyEqualTo(param);
		Page<Publckrec> publckrecs = basePublckrecService.selectByExample(publckrecExample);

		if (publckrecs.size() == 0) {
			if (localLogger.isInfoEnabled()) {
				localLogger.info("AppTrylock: 没有加锁或已经被解锁! sRecKey=[" + param + "]");
			}
			return 0;
		}

		long curLockTime = System.currentTimeMillis();
		String lockTimeStr = StringUtils.substring(String.valueOf(curLockTime), 0, 10);
		curLockTime = Long.parseLong(lockTimeStr);

		Publckrec publckrec = publckrecs.get(0);
		String oldLckTimStr = publckrec.getLcktim();
		String oldTimOutStr = publckrec.getTimout();

		long oldLckTim = Long.parseLong(oldLckTimStr.trim());
		long oldTimOut = Long.parseLong(oldTimOutStr.trim());

		if (oldLckTim + oldTimOut > curLockTime) {
			if (localLogger.isInfoEnabled()) {
				localLogger.info("AppTrylock: 加锁中! sRecKey=[" + param + "]");
			}
			return 1;
		}

		if (localLogger.isInfoEnabled()) {
			localLogger.info("AppTrylock: 锁已经超时失效! sRecKey=[" + param + "]");
		}
		return 0;
	}

	/**
	 * 加锁
	 * 
	 * @param RecKeyParam
	 * @param timOutparam
	 * @return
	 */
	@Transactional
	public Integer appLock(String RecKeyParam, String timOutparam) {

		long l1 = System.currentTimeMillis();
		String str1 = StringUtils.substring(String.valueOf(l1), 0, 10);

		l1 = Long.parseLong(str1);

		RecKeyParam = RecKeyParam.trim();

		Publckrec publckrec = new Publckrec();

		int i = 0;
		publckrec.setReckey(RecKeyParam);
		publckrec.setUpdflg("1");
		i = basePublckrecService.updateByPrimaryKeySelective(publckrec);

		if (i == 0) {
			publckrec.setLcktim(str1);
			publckrec.setTimout(timOutparam);
			int j = basePublckrecService.insert(publckrec);
			if (j == 0) {
				if (localLogger.isInfoEnabled()) {
					localLogger.info("AppLock: 登记锁失败. aRecKey=[" + RecKeyParam + "] aLckTim=[" + l1 + "] aTimOut=[" + timOutparam + "]");
				}
				i = 2;
				return i;
			}
			if (localLogger.isInfoEnabled())
				localLogger.info("AppLock: 登记锁成功. aRecKey=[" + RecKeyParam + "] aLckTim=[" + l1 + "] aTimOut=[" + timOutparam + "]");
		} else {

			PublckrecExample publckrecExample = new PublckrecExample();
			PublckrecExample.Criteria criteria = publckrecExample.createCriteria();
			criteria.andReckeyEqualTo(RecKeyParam);
			Page<Publckrec> publckrecs = basePublckrecService.selectByExample(publckrecExample);

			publckrec = publckrecs.get(0);

			String str3 = publckrec.getLcktim();
			String str4 = publckrec.getTimout();
			if (str3 == null) {
				str3 = "0";
			}
			if (str4 == null) {
				str4 = "0";
			}
			long l2 = Long.parseLong(str3.trim());
			long l3 = Long.parseLong(str4.trim());
			if ((l1 - l2 < l3) || (l3 == 0L)) {
				localLogger.error("AppLock: 加锁失败. aRecKey=[" + RecKeyParam + "] aLckTim=[" + l1 + "] aOldLckTim=[" + str3 + "] aOldTimOut=[" + str4 + "]");
				// 手动回滚事务
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				return 2;
			}
			publckrec.setLcktim(str1);
			publckrec.setTimout(timOutparam);
			publckrec.setReckey(RecKeyParam);
			basePublckrecService.insert(publckrec);
			if (localLogger.isInfoEnabled()) {
				localLogger.info("AppLock: 加锁成功. aRecKey=[" + RecKeyParam + "] aLckTim=[" + l1 + "] aOldLckTim=[" + str3 + "] aOldTimOut=[" + str4 + "]");
			}
			i = 0;
		}
		return i;
	}

	/**
	 * 释放锁
	 * 
	 * @param RecKeyParam
	 * @return
	 */
	public Integer unAppLock(String RecKeyParam) {
		int i = 0;
		RecKeyParam = RecKeyParam.trim();
		PublckrecExample publckrecExample = new PublckrecExample();
		PublckrecExample.Criteria criteria = publckrecExample.createCriteria();
		criteria.andReckeyEqualTo(RecKeyParam);
		i = basePublckrecService.deleteByExample(publckrecExample);
		if (i == 0) {
			if (localLogger.isInfoEnabled()) {
				localLogger.info("AppUnlock: 没有加锁或已经被解锁! sRecKey=[" + RecKeyParam + "]");
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			}
			i = 1;
		}
		return 0;
	}
}