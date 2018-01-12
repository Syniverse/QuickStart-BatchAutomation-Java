package com.syniverse.eis.aba.sample.schedule;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 * calls mss file creation service to create mss file. 
 * calls mss file upload service to upload data
 * calls aba schedule service to initiate ABA job.
 * repeatedly call the aba get execution details request for the freshly created ABA job until it's done
 */
public class CreateScheduleApp {

	public static void main(String[] args) throws IOException, InterruptedException {
		ResourceBundle appProperties = ResourceBundle.getBundle("application");
		CloseableHttpClient httpClient = HttpClients.createDefault();
		String jobType = appProperties.getString("sample.job.type"); 
		
		// create MSS file (ABA input file)
		HttpPost defineFileRequest = new HttpPost(appProperties.getString("mss.host") + "/mediastorage/v1/files");
		defineFileRequest.addHeader("Content-Type", "application/json");
		defineFileRequest.addHeader("Authorization", appProperties.getString("user.sdc.bearer-token"));
		defineFileRequest.setEntity(new StringEntity(FileUtils.readFileToString(new File("src/main/resources/mss-file-definition.json"), "UTF-8")));
		CloseableHttpResponse defineFileResponse = httpClient.execute(defineFileRequest);
		HttpEntity defineFileResponseEntity = defineFileResponse.getEntity();
		String defineFileResponseString = IOUtils.toString(defineFileResponseEntity.getContent(), "UTF-8");
		System.out.println("MSS file creation response: " + defineFileResponseString);
		JSONObject defineFileResponseJson = new JSONObject(defineFileResponseString);
		String mssFileId = defineFileResponseJson.getString("file_id");
		String companyId = defineFileResponseJson.getString("company-id");
		EntityUtils.consume(defineFileResponseEntity);
		defineFileResponse.close();

		// upload file content to newly created MSS file.
		HttpPost uploadFileRequest = new HttpPost(appProperties.getString("mss.host") + "/mediastorage/v1/files/" + mssFileId + "/content");
		uploadFileRequest.addHeader("Content-Type", "application/octet-stream");
		uploadFileRequest.addHeader("Authorization", appProperties.getString("user.sdc.bearer-token"));
		uploadFileRequest.addHeader("int-companyid", companyId);
		uploadFileRequest.setEntity(new StringEntity(FileUtils.readFileToString(new File("src/main/resources/" + jobType + "/mss-input.txt"), "UTF-8")));
		CloseableHttpResponse uploadFileResponse = httpClient.execute(uploadFileRequest);
		System.out.println("MSS file upload response status code: " +  uploadFileResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(uploadFileResponse.getEntity());
		uploadFileResponse.close();

		// create aba job schedule
		HttpPost createScheduleRequest = new HttpPost(appProperties.getString("aba.host") + "/aba/v1/schedules");
		createScheduleRequest.addHeader("Content-Type", "application/json");
		createScheduleRequest.addHeader("Authorization", appProperties.getString("user.sdc.bearer-token"));
		createScheduleRequest.addHeader("int-companyid", companyId);
		String scheduleRequestBody = FileUtils.readFileToString(new File("src/main/resources/" + jobType + "/aba-schedule.json"), "UTF-8");
		scheduleRequestBody = scheduleRequestBody.replace("<inputFileId>", mssFileId);
		scheduleRequestBody = scheduleRequestBody.replace("<deliveryConfigurationId>", appProperties.getString("ess.delivery.configuration.id"));
		createScheduleRequest.setEntity(new StringEntity(scheduleRequestBody));
		CloseableHttpResponse createScheduleResponse = httpClient.execute(createScheduleRequest);
		HttpEntity createScheduleResponseEntity = createScheduleResponse.getEntity();
		String createScheduleResponseString = IOUtils.toString(createScheduleResponseEntity.getContent(), "UTF-8");
		System.out.println("ABA schedule creation response: " + createScheduleResponseString);
		JSONObject createScheduleResponseJson = new JSONObject(createScheduleResponseString);
		String scheduleId = createScheduleResponseJson.getJSONObject("schedule").getString("id");
		EntityUtils.consume(createScheduleResponseEntity);
		createScheduleResponse.close();
		
		// periodically check status until the job is done
		while (true) {
			Thread.sleep(10000);
			HttpGet getExecutionsRequest = new HttpGet(appProperties.getString("aba.host") + "/aba/v1/schedules/" + scheduleId + "/executions");
			getExecutionsRequest.addHeader("Content-Type", "application/json");
			getExecutionsRequest.addHeader("Authorization", appProperties.getString("user.sdc.bearer-token"));
			getExecutionsRequest.addHeader("int-companyid", companyId);
			CloseableHttpResponse getExecutionsResponse = httpClient.execute(getExecutionsRequest);
			HttpEntity getExecutionsResponseEntity = getExecutionsResponse.getEntity();
			String getExecutionsResponseString = IOUtils.toString(getExecutionsResponseEntity.getContent(), "UTF-8");
			System.out.println("ABA execution current state: " + getExecutionsResponseString);
			JSONObject getExecutionsResponseJson = new JSONObject(getExecutionsResponseString);
			String status = getExecutionsResponseJson.getJSONArray("executions").getJSONObject(0).getString("status");
			EntityUtils.consume(getExecutionsResponseEntity);
			getExecutionsResponse.close();
			if ("COMPLETE".equals(status) || "FAILED".equals(status)) {
				System.out.println("Execution finished with status " + status);
				break;
			}
		}
	}
}
