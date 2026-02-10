package ec.com.sidesoft.happypay.web.services.ad_process;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.database.SessionInfo;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DalBaseProcess;
import org.openbravo.service.db.QueryTimeOutUtil;

import ec.com.sidesoft.actuaria.special.customization.Scactu_Log;
import ec.com.sidesoft.happypay.web.services.shppws_config;
import org.openbravo.base.exception.OBException;

public class DaysToCleanLog extends DalBaseProcess{

	private static ProcessLogger logger;
	\





	
	@Override
	protected void doExecute(ProcessBundle bundle) throws Exception {
		// TODO Auto-generated method stub
		logger = bundle.getLogger();
		ConnectionProvider conn = bundle.getConnection();
		try {
			OBCriteria<shppws_config> queryUrl= OBDal.getInstance().createCriteria(shppws_config.class);
		    List<shppws_config> listUrl= queryUrl.list();
		    
		    if(listUrl.size() > 0) {
		    	Long daysValidatorAux = listUrl.get(0).getDaysCleanlog();
		    	int daysValidator = daysValidatorAux != null ? daysValidatorAux.intValue() : 0;
		    	if(daysValidator != 0 && daysValidator > 0) {
		    		// fecha límite
		    	    LocalDate limitDate = LocalDate.now().minusDays(daysValidator);
		    	    
		    	    Date limitDateAsDate = Date.from(limitDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		    	    
		    	    delete(conn, limitDateAsDate);
		    		
		    	}else {
		    		logger.logln("No hay coincidencia de registros para ser eliminados.");
		    	}
		    }else {
		    	logger.logln("No hay coincidencia de registros para ser eliminados.");
		    }
		}catch(Exception e) {
			logger.logln("No se ha podido completar el proceso /" + e.getMessage());
			throw new OBException("Error al limpiar los registros de la tabla log de servicios web.");
		}
	}
	
	public void delete(ConnectionProvider conn, Date date) {
	    logger.logln("Eliminando registros con fecha menor a "+date+" :");
	    int updateCount = 0;

	    String sql = "DELETE FROM scactu_log WHERE created < ?";

	    PreparedStatement st = null;
	    try {
	        st = conn.getPreparedStatement(sql);
	        st.setDate(1, new java.sql.Date(date.getTime())); // Convertimos java.util.Date a java.sql.Date
	        QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
	        updateCount = st.executeUpdate();

	        if (updateCount > 0) {
	        	logger.logln("Depuración exitosa de logs. Registros eliminados: " + updateCount);
	        } else {
	            logger.logln("No se encontraron registros para la depuración de logs");
	        }
	    } catch (Exception e) {
	        logger.logln("Error ejecutando delete: " + sql + " - " + e.getMessage());
	    } finally {
	        try {
	            if (st != null) st.close();
	        } catch (Exception e) {
	            logger.logln("Error cerrando PreparedStatement: " + e.getMessage());
	        }
	    }
	}


}
