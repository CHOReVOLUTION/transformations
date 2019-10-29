package ${bcData.packagename}.business.impl;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityHelper {
	
	private static Logger logger = LoggerFactory.getLogger(SecurityHelper.class);
	
	static private long calculateNormalizedTimeFromBegin()
	{	
		Calendar begin = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
		begin.set(1970, 0, 1, 0, 0, 0);
		begin.set(Calendar.MILLISECOND, 0);
		
		long deltaSeconds = 15 * 60;
		Calendar currentDate = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));

		long elapsedTime = currentDate.getTimeInMillis() - begin.getTimeInMillis();
		double elapsedSeconds = elapsedTime / 1000;
		double elapsedNormalized = elapsedSeconds / deltaSeconds;

		BigDecimal bd = new BigDecimal(elapsedNormalized);
		bd = bd.setScale(0,BigDecimal.ROUND_HALF_UP);
		double elapsedNormalizedRounded = bd.doubleValue();
		return (long)elapsedNormalizedRounded * deltaSeconds;
	}

	static public String getDynamicPassword(String password)
	{
        try {
            long now = calculateNormalizedTimeFromBegin();
            String calculatedPwd = now + password;

            return SecurityHelper.computeHash(calculatedPwd);
        } catch (Exception e) {
        	
           logger.info("SECURITY", "PWD ERROR", e);
        }
        return null;
	}
	
	public static String computeHash(String x) throws Exception  
	{
		java.security.MessageDigest d = null;
		d = java.security.MessageDigest.getInstance("SHA-1");
		d.reset();
		d.update(x.getBytes("UTF-8"));
		
		return  byteArrayToHexString(d.digest());
	}

	private static String byteArrayToHexString(byte[] b){
		StringBuffer sb = new StringBuffer(b.length * 2);
		for (int i = 0; i < b.length; i++){
			int v = b[i] & 0xff;
			if (v < 16) {
				sb.append('0');
			}
			sb.append(Integer.toHexString(v));
		}
		return sb.toString().toLowerCase(Locale.US);
	}
}
