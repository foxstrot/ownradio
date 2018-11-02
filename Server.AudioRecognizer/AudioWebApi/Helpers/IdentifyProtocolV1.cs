using Serilog;
using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Net.Http;
using System.Security.Cryptography;
using System.Text;

namespace AudioWebApi
{
    public class IdentifyProtocolV1
    {
        private string PostHttp(string url, IDictionary<string, Object> postParams, int timeout)
        {
            string result = "";

            string BOUNDARYSTR = "acrcloud***copyright***2015***" + DateTime.Now.Ticks.ToString("x");
            string BOUNDARY = "--" + BOUNDARYSTR + "\r\n";
            var ENDBOUNDARY = Encoding.ASCII.GetBytes("--" + BOUNDARYSTR + "--\r\n\r\n");

            var stringKeyHeader = BOUNDARY +
                           "Content-Disposition: form-data; name=\"{0}\"" +
                           "\r\n\r\n{1}\r\n";
            var filePartHeader = BOUNDARY +
                            "Content-Disposition: form-data; name=\"{0}\"; filename=\"{1}\"\r\n" +
                            "Content-Type: application/octet-stream\r\n\r\n";

            var memStream = new MemoryStream();
            foreach (var item in postParams)
            {
                if (item.Value is string)
                {
                    string tmpStr = string.Format(stringKeyHeader, item.Key, item.Value);
                    byte[] tmpBytes = Encoding.UTF8.GetBytes(tmpStr);
                    memStream.Write(tmpBytes, 0, tmpBytes.Length);
                }
                else if (item.Value is byte[])
                {
                    var header = string.Format(filePartHeader, "sample", "sample");
                    var headerbytes = Encoding.UTF8.GetBytes(header);
                    memStream.Write(headerbytes, 0, headerbytes.Length);
                    byte[] sample = (byte[])item.Value;
                    memStream.Write(sample, 0, sample.Length);
                    memStream.Write(Encoding.UTF8.GetBytes("\r\n"), 0, 2);
                }
            }
            memStream.Write(ENDBOUNDARY, 0, ENDBOUNDARY.Length);

            Log.Information("Write post params");

            HttpClient httpClient = new HttpClient();
            try
            {
                httpClient.Timeout = TimeSpan.FromSeconds(timeout);

                memStream.Position = 0;
                byte[] tempBuffer = new byte[memStream.Length];
                memStream.Read(tempBuffer, 0, tempBuffer.Length);

                ByteArrayContent byteContent = new ByteArrayContent(tempBuffer);
                byteContent.Headers.Add("Content-Type", "multipart/form-data; boundary=" + BOUNDARYSTR);

                Log.Information("Send request");
                var response = httpClient.PostAsync(url, byteContent).Result;
                result = response.Content.ReadAsStringAsync().Result;

                Log.Information("Read response");
            }
            catch (WebException e)
            {
                Log.Error("Timeout");
                throw new Exception("timeout:\n" + e.ToString());
            }
            catch (Exception e)
            {
                Log.Error($"Exception{e.ToString()}");
                throw new Exception("other excption:" + e.ToString());
            }

            return result;
        }

        private string EncryptByHMACSHA1(string input, string key)
        {
            using (HMACSHA1 hmac = new HMACSHA1(Encoding.UTF8.GetBytes(key)))
            {
                byte[] stringBytes = Encoding.UTF8.GetBytes(input);
                byte[] hashedValue = hmac.ComputeHash(stringBytes);
                return EncodeToBase64(hashedValue);
            }
        }

        private string EncodeToBase64(byte[] input)
        {
            return Convert.ToBase64String(input, 0, input.Length);
        }

        public string Recognize(string host, string accessKey, string secretKey, byte[] queryData, string queryType, int timeout = 20)
        {
            string method = "POST";
            string httpURL = "/v1/identify";
            string dataType = queryType;
            string sigVersion = "1";
            string timestamp = DateTimeOffset.UtcNow.ToUnixTimeSeconds().ToString();
            var tmp = ((int)DateTime.UtcNow.Subtract(new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc)).TotalSeconds).ToString();

            string reqURL = "http://" + host + httpURL;

            string sigStr = method + "\n" + httpURL + "\n" + accessKey + "\n" + dataType + "\n" + sigVersion + "\n" + timestamp;
            string signature = EncryptByHMACSHA1(sigStr, secretKey);

            var dict = new Dictionary<string, object>
            {
                { "access_key", accessKey },
                { "sample_bytes", queryData.Length.ToString() },
                { "sample", queryData },
                { "timestamp", timestamp },
                { "signature", signature },
                { "data_type", queryType },
                { "signature_version", sigVersion }
            };

            Log.Information("Generate post params");

            string res = PostHttp(reqURL, dict, timeout);

            return res;
        }
    }
}
