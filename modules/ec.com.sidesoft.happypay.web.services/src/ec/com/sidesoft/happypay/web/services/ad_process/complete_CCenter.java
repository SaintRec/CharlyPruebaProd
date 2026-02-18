package ec.com.sidesoft.happypay.web.services.ad_process;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.openbravo.service.db.DbUtility;

import ec.com.sidesoft.credit.factory.SscfCom1;
import ec.com.sidesoft.credit.factory.SscfCreditOperation;
import ec.com.sidesoft.credit.simulator.scsl_Product;

public class complete_CCenter extends DalBaseProcess {
	// nombre de la clase en java
	// ec.com.sidesoft.happypay.web.services.ad_process.complete_CCenter
	// private final Logger logger = Logger.getLogger(Sbc_Reactivate.class);
	@Override
	public void doExecute(ProcessBundle bundle) throws Exception {
		OBError msg = new OBError();
		OBContext.setAdminMode(true);

		try {
			String idScom1 = (String) bundle.getParams().get("Sscf_Com1_ID");
			SscfCom1 objCOM = OBDal.getInstance().get(SscfCom1.class, idScom1);
			String observation = "Completado";

			String idCreditOperation = objCOM.getSscfCreditOperation().getId();
			SscfCreditOperation objCreditOperation = OBDal.getInstance().get(SscfCreditOperation.class,
					idCreditOperation);

			String from_artboard = null; // COM1
			String to_artboard = (String) bundle.getParams().get("artboard"); // COM1
			if (to_artboard == null) {
				to_artboard = "CC";
			}
			String CallCenter = objCreditOperation.getCallCenterStatus();
			String SCom = objCreditOperation.getSComStatus();
			String COM2 = objCreditOperation.getCom2Status();
			String COM1 = objCreditOperation.getCom1Status();
			if (CallCenter != null && (CallCenter.equals("O") || CallCenter.equals("G") || CallCenter.equals("Y")
					|| CallCenter.equals("R"))) {
				from_artboard = "CC";
			} else if (SCom != null && (SCom.equals("O") || SCom.equals("G") || SCom.equals("Y") || SCom.equals("R"))) {
				from_artboard = "S-COM";
			} else if (COM2 != null && (COM2.equals("O") || COM2.equals("G") || COM2.equals("Y") || COM2.equals("R"))) {
				from_artboard = "COM2";
			} else if (COM1 != null && (COM1.equals("O") || COM1.equals("G") || COM1.equals("Y") || COM1.equals("R"))) {
				from_artboard = "COM1";
			}

			objCreditOperation.setCallCenterStatus("G");
			objCreditOperation.setDocumentStatus("A");
			objCreditOperation.setShppwsObservation("");
			OBDal.getInstance().save(objCreditOperation);
			OBDal.getInstance().flush();

			if (from_artboard != null && to_artboard != null) {
				new_binnacle_opcredit binnacle = new new_binnacle_opcredit();
				binnacle.createBinnacle(objCreditOperation, from_artboard, to_artboard,
						objCreditOperation.getDocumentStatus(), observation);

			}

			msg.setType("Success");
			msg.setTitle(OBMessageUtils.messageBD("Success"));
			msg.setMessage("Se ha completado la operación con éxito");
		} catch (final Exception e) {
			msg.setType("Error");
			msg.setTitle(OBMessageUtils.messageBD("Error"));
			msg.setMessage(" No se logró completar la operación");

		} finally {
			OBContext.restorePreviousMode();
			bundle.setResult(msg);
		}
	}

}