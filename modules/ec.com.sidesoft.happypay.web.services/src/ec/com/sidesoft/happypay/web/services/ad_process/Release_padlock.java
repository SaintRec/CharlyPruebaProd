package ec.com.sidesoft.happypay.web.services.ad_process;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.model.ad.system.SystemInformation;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DalBaseProcess;
import org.openbravo.service.db.DalConnectionProvider;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ec.com.sidesoft.actuaria.special.customization.Scactu_Log;
import ec.com.sidesoft.happypay.web.services.shppws_config;
import ec.com.sidesoft.happypay.web.services.service.log_records;
import it.openia.crm.Opcrmopportunities;

public class Release_padlock extends DalBaseProcess {
	private static ProcessLogger logger;
	ConnectionProvider conn = new DalConnectionProvider(false);

	@Override
	protected void doExecute(ProcessBundle bundle) throws Exception {
		// TODO Auto-generated method stub
		logger = bundle.getLogger();
		padlockReleaseOperation();
	}

	public void padlockReleaseOperation() {

		String instance = "D";
		try {
			OBContext.setAdminMode();
			OBCriteria<SystemInformation> queryApi = OBDal.getInstance().createCriteria(SystemInformation.class);
			queryApi.setMaxResults(1);
			SystemInformation accesInfo = (SystemInformation) queryApi.uniqueResult();
			instance = accesInfo.getInstancePurpose();

		} finally {
main
			OBContext.restorePreviousMode();
		}

		if (!instance.equals("P")) {
			logger.logln("No es una instancia de produccion");
			throw new OBException("No es una instancia de produccion");
		}

		try {
			OBCriteria<shppws_config> queryApi = OBDal.getInstance().createCriteria(shppws_config.class);
			shppws_config accesApi = (shppws_config) queryApi.uniqueResult();

			/*
			 * final OBCriteria<DocumentType> queryDocumenType=
			 * OBDal.getInstance().createCriteria(DocumentType.class);
			 * queryDocumenType.add(Restrictions.eq(DocumentType.PROPERTY_DOCUMENTCATEGORY,
			 * "ARI"));
			 * queryDocumenType.add(Restrictions.eq(DocumentType.PROPERTY_SHPFRISCREDITOPE,
			 * true));
			 * List<DocumentType> listDocType = queryDocumenType.list();
			 * String[] docTypeIds = new String[listDocType.size()]; // Creamos un array del
			 * tamaño adecuado
			 * int index = 0;
			 * for (DocumentType docType : listDocType) {
			 * docTypeIds[index] = docType.getId();
			 * index++;
			 * }
			 */

			ReleasePadLockData[] headerData = ReleasePadLockData.selectRelease(conn);
			String cinvoiceid = "";
			String invoicedoc = "";
			String docopp = "";
			/*
			 * final OBCriteria<Invoice> queryInvoice=
			 * OBDal.getInstance().createCriteria(Invoice.class);
			 * queryInvoice.add(Restrictions.in(Invoice.PROPERTY_TRANSACTIONDOCUMENT +
			 * ".id", docTypeIds));//ids
			 * queryInvoice.add(Restrictions.eq(Invoice.PROPERTY_OUTSTANDINGAMOUNT,
			 * BigDecimal.ZERO));
			 * queryInvoice.add(Restrictions.eq(Invoice.PROPERTY_SHPICOPERATIONSTATE,"05"));
			 * //CANCELADO
			 * queryInvoice.add(Restrictions.eq(Invoice.PROPERTY_SHPICLOCKSTATUS,"001"));//
			 * 001->DESBLOQUEADO //002->BLOQUEADO
			 * queryInvoice.add(Restrictions.eq(Invoice.PROPERTY_SHPPWSPADLOCKRELEASED,false
			 * ));
			 * List<Invoice> listInvoices = queryInvoice.list();
			 * int sizeList = listInvoices.size();
			 */
			if (headerData.length > 0) {
				for (ReleasePadLockData data : headerData) {
					cinvoiceid = data.cInvoiceId;
					invoicedoc = data.documentno;
					docopp = data.emShppwsOpDocumentno;
					/*
					 * OBCriteria<Opcrmopportunities> queryOpportunity =
					 * OBDal.getInstance().createCriteria(Opcrmopportunities.class);
					 * queryOpportunity.add(Restrictions.eq(Opcrmopportunities.
					 * PROPERTY_SHPPWSOPDOCUMENTNO, objInvoice.getDocumentNo()));
					 * List<Opcrmopportunities> listobjOpportunity = queryOpportunity.list();
					 * if(listobjOpportunity.size()>0) {
					 * Opcrmopportunities objOpportunity = listobjOpportunity.get(0);
					 */

					Boolean validate = false;// Valid Execution
					validate = getApiResponsePadlockRelease(accesApi, docopp);

					if (validate) {// EJECUCIÓN
						logger.logln("DocumentNo:" + invoicedoc + " Release: true");
						// objInvoice.setShppwsPadlockReleased(true);//LIBERAR
					} else {
						logger.logln("DocumentNo:" + invoicedoc + " Release: false");
					}
				}

			}

			logger.logln("Padlock Release Successful.");
		} catch (Exception e) {
			logger.logln("Padlock Release failed." + e.getMessage());
		}
	}

	public boolean getApiResponsePadlockRelease(shppws_config accesApi, String Identifier) throws Exception {
		log_records logger = new log_records();

		String apiUrl = "";
		String apiEndPoint = "ResponsePadlockRelease";
		String apiTypeAuth = "";
		String apiUser = "";
		String apiPass = "";
		String apiToken = "";
		String Depurador = "";

		apiUrl = accesApi.getReleaseNamespace();
		apiEndPoint = accesApi.getReleaseReadEndpoint();
		apiTypeAuth = accesApi.getReleaseTypeAuth();
		apiUser = accesApi.getReleaseUser();
		apiPass = accesApi.getReleasePass();
		apiToken = accesApi.getReleaseToken();

		Scactu_Log log = logger.log_start_register(accesApi, apiEndPoint, null);

		int responseCode = 500;
		HttpURLConnection connectionhttp = null;
		HttpsURLConnection connectionhttps = null;
		HttpURLConnection connection = null;
		JSONObject requestBody = new JSONObject();
		String apiResponse = "";

		// BA -> Basic auth
		// TA -> Token auth
		if (apiTypeAuth.equals("BA")) {
			URL url = new URL(apiUrl + apiEndPoint);
			connectionhttp = (HttpURLConnection) url.openConnection();
			connectionhttp.setRequestMethod("GET");
			String username = apiUser;
			String password = apiPass;
			String authString = username + ":" + password;
			String authHeaderValue = "Basic " + Base64.getEncoder().encodeToString(authString.getBytes());
			connectionhttp.setRequestProperty("Authorization", authHeaderValue);

			// Obtiene la respuesta de la API
			responseCode = connectionhttp.getResponseCode();
			connection = connectionhttp;
		} else if (apiTypeAuth.equals("AT")) {
			// Deshabilitar la validación de certificados SSL
			TrustManager[] trustAllCerts = new TrustManager[] {
					new X509TrustManager() {
						public java.security.cert.X509Certificate[] getAcceptedIssuers() {
							return null;
						}

						public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
						}

						public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
						}
					}
			};

			// Configurar SSLContext con la configuración personalizada
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			// Obtener la conexión HTTPS y aplicar la configuración personalizada
			URL url = new URL(apiUrl + apiEndPoint);
			connectionhttps = (HttpsURLConnection) url.openConnection();
			// Desactivar la verificación estricta del nombre del host
			connectionhttps.setHostnameVerifier((hostname, session) -> true);
			connectionhttps.setSSLSocketFactory(sslContext.getSocketFactory());
			connectionhttps.setRequestMethod("POST");

			String authHeaderValue = "Bearer " + apiToken;
			connectionhttps.setRequestProperty("Authorization", authHeaderValue);

			requestBody.put("OpportunityNumber", Identifier);

			log = logger.log_setValues(log, requestBody.toString());

			connectionhttps.setRequestProperty("Content-Type", "application/json");

			connectionhttps.setDoOutput(true);
			OutputStreamWriter writer = new OutputStreamWriter(connectionhttps.getOutputStream());
			writer.write(requestBody.toString());
			writer.flush();
			writer.close();
			Depurador = Depurador + "Obtiene la respuesta ";
			// Obtiene la respuesta de la API
			responseCode = connectionhttps.getResponseCode();
			connection = connectionhttps;
		} else if (apiTypeAuth.equals("OA")) {
			// Configura los datos de autenticación OAuth 2.0
			String tokenUrl = accesApi.getTTNParams();
			String clientId = accesApi.getTTNUser();
			String clientSecret = accesApi.getTTNPass();
			String scope = "api1";

			try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
				// Obtén un token OAuth 2.0
				String oauthToken = getOAuth2Token(httpClient, tokenUrl, clientId, clientSecret, scope);

				if (oauthToken != null) {
					HttpPut httpPut = new HttpPut(apiUrl + apiEndPoint);

					requestBody.put("OpportunityNumber", Identifier);

					// Body
					StringEntity entity = new StringEntity(requestBody.toString());
					entity.setContentType("application/json");
					httpPut.setEntity(entity);

					// Configura el encabezado de autorización con el token OAuth 2.0
					httpPut.setHeader("Authorization", "Bearer " + oauthToken);

					// Realiza la solicitud POST
					try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
						// Procesa la respuesta
						responseCode = response.getStatusLine().getStatusCode();
						HttpEntity responseEntity = response.getEntity();
						apiResponse = EntityUtils.toString(responseEntity);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		String noReference = Identifier;
		String Interface = "SHPPWS_NT";
		String Process = "Liberacion del Candado: " + Identifier;
		String idRegister = "";
		String Error = "" + apiResponse;

		if (responseCode == HttpURLConnection.HTTP_OK) {
			if (apiResponse.equals("") || apiResponse == null) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				StringBuilder responseBuilder = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					responseBuilder.append(line);
				}
				reader.close();
				apiResponse = responseBuilder.toString();
			}

			try {
				logger.log_end_register(log, apiUrl, noReference, apiResponse, "OK", "OUT", Interface, Process,
						idRegister, Error);
				return true;

			} catch (Exception e) {
				logger.log_end_register(log, apiUrl, noReference, apiResponse, "ERROR", "OUT", Interface, Process,
						idRegister, Error);
				return false;
			}

		} else {
			logger.log_end_register(log, apiUrl, noReference, "Response Code " + responseCode, "ERROR", "OUT",
					Interface, Process, idRegister, Error);
		}
		return false;
	}

	public String getOAuth2Token(CloseableHttpClient httpClient, String tokenUrl, String clientId, String clientSecret,
			String scope) {
		try {
			// New Post
			HttpPost httpPost = new HttpPost(tokenUrl);

			// Datos de autenticación en el cuerpo de la solicitud
			List<NameValuePair> params = new ArrayList<>();
			params.add(new BasicNameValuePair("grant_type", "client_credentials"));
			params.add(new BasicNameValuePair("client_id", clientId));
			params.add(new BasicNameValuePair("client_secret", clientSecret));
			params.add(new BasicNameValuePair("scope", scope));
			httpPost.setEntity(new UrlEncodedFormEntity(params));

			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();

			// Get token if code = 200
			if (response.getStatusLine().getStatusCode() == 200) {
				String responseBody = EntityUtils.toString(entity);
				Gson gson = new Gson();
				JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
				String accessToken = jsonResponse.get("access_token").getAsString();
				return accessToken;
			} else {
				return "";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

}
