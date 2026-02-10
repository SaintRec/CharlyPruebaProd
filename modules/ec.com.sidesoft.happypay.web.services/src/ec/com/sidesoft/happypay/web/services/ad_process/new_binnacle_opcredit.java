package ec.com.sidesoft.happypay.web.services.ad_process;

import java.sql.SQLException;
import java.util.List;

import org.apache.tools.ant.types.resources.comparators.Size;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;

import ec.com.sidesoft.credit.factory.SscfBinnacle;
import ec.com.sidesoft.credit.factory.SscfCreditOperation;

import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;

public class new_binnacle_opcredit {

	public OBError createBinnacle(SscfCreditOperation objCreditOperation, String from, String until, String event,
			String observation) {
		OBError msg = new OBError();
		// OBContext.setAdminMode(true);
		OBCriteria<SscfBinnacle> queryBinnacles = OBDal.getInstance().createCriteria(SscfBinnacle.class);
		queryBinnacles.add(Restrictions.eq(SscfBinnacle.PROPERTY_SSCFCREDITOPERATION, objCreditOperation));
		List<SscfBinnacle> listBinnacles = queryBinnacles.list();
		int numLine = (listBinnacles.size() + 1) * 10;

		try {
			Client client = OBContext.getOBContext().getCurrentClient();
			Organization organization = OBContext.getOBContext().getCurrentOrganization();
			User user = OBContext.getOBContext().getUser();
			SscfBinnacle newBinnacle = OBProvider.getInstance().get(SscfBinnacle.class);
			newBinnacle.setClient(client);
			newBinnacle.setOrganization(organization);
			newBinnacle.setUser(user);
			newBinnacle.setSscfCreditOperation(objCreditOperation);
			newBinnacle.setArtboardFrom(from);
			newBinnacle.setArtboardTo(until);
			newBinnacle.setLineNo(new Long(numLine));
			newBinnacle.setEvent(event);
			newBinnacle.setComments(observation);

			OBDal.getInstance().save(newBinnacle);
			OBDal.getInstance().flush();
			OBDal.getInstance().refresh(newBinnacle);
			OBDal.getInstance().getConnection().commit();
			msg.setType("Success");
		} catch (Exception e) {
			msg.setType("Error");
		}

		return msg;
	}

}
