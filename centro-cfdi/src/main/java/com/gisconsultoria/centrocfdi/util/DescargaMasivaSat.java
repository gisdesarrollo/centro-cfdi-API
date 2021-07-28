package com.gisconsultoria.centrocfdi.util;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.security.auth.x500.X500Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gisconsultoria.centrocfdi.services.GetCfdiSatService;

@Service
public class DescargaMasivaSat implements IDescargaMasivaSat {

	private static final Logger LOG = LoggerFactory.getLogger(DescargaMasivaSat.class);

	@Autowired
	private GetCfdiSatService cfdiSatService;

	@Autowired
	private IMessagesSat messagesSat;

	@Override
	public String getAuthenticacion(UUID uuid, String fechaInicial, String fechaFinal, byte[] cerByte, byte[] keyByte,
			String password) {

		String encodeCer = null;
		String messageXml = null;
		try {
			// get messageDigesvalue
			String messageDV = messagesSat.getMessageDigestValueAuthenticacion(fechaInicial, fechaFinal);
			// get digesValue
			String digesValue = cfdiSatService.getDigesValue(messageDV);
			// get messageSignatureValue
			String messageSV = messagesSat.getMessageSignatureValueAuthenticacion(digesValue);
			// get privateKey
			PrivateKey privateKey = cfdiSatService.getPrivateKey(keyByte, password);
			// encode cer
			encodeCer = Base64.getEncoder().encodeToString(cerByte);
			System.out.println("authenticacion CER: " + encodeCer);
			// get certficado
			X509Certificate cer = cfdiSatService.getCertificate(cerByte);

			int pathLen = cer.getBasicConstraints();
			if (pathLen != -1) {
				LOG.error("El certificado no es un CSD, posee el atributo de Autoridad Certificadora (CA)");
				throw new RuntimeException(
						"El certificado no es un CSD, posee el atributo de Autoridad Certificadora (CA)");
			}
			String signatureValue = null;

			// get signatureValue
			signatureValue = cfdiSatService.getSignatureValue(privateKey, digesValue, messageSV);

			// verifica llaves correspondientes
			if (!cfdiSatService.verify(messageSV, signatureValue, cer)) {
				throw new RuntimeException("la llave privada y el certificado no corresponden");
			}
			// get message authenticacion
			messageXml = messagesSat.getMessageAuthenticacion(uuid, fechaInicial, fechaFinal, digesValue,
					signatureValue, encodeCer);
		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
			e.printStackTrace();
			LOG.error("Error al momento de obtener el signature value:" + e);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Error al momento de ejecución");
		}
		return messageXml;
	}

	@Override
	public String getSolicitud(String fechaInicial, String fechaFinal, String rfcEmisor, String tipoSolicitud,
			byte[] cerByte, byte[] keyByte, String password) {

		String encodeCer = null;
		Principal datosCertificado = null;
		BigInteger numCertificado = null;
		String messageXml = null;
		try {
			// encode cer
			encodeCer = Base64.getEncoder().encodeToString(cerByte);
			// get certficado
			X509Certificate cer = cfdiSatService.getCertificate(cerByte);
			int pathLen = cer.getBasicConstraints();
			if (pathLen != -1) {
				LOG.error("El certificado no es un CSD, posee el atributo de Autoridad Certificadora (CA)");
				throw new RuntimeException(
						"El certificado no es un CSD, posee el atributo de Autoridad Certificadora (CA)");
			}
			numCertificado = cer.getSerialNumber();
			datosCertificado = cer.getIssuerDN();

			// get messageDigesValue
			String messageDV = messagesSat.getMessageDigestValueSolicitud(rfcEmisor, fechaInicial, fechaFinal,
					tipoSolicitud);
			// get digesValue
			String digesValue = cfdiSatService.getDigesValue(messageDV);
			// get messageSignatureValue
			String messageSV = messagesSat.getMessageSignatureValueSolicitud(digesValue);
			// get privateKey
			PrivateKey privateKey = cfdiSatService.getPrivateKey(keyByte, password);
			String signatureValue = null;
			// get signatureValue
			signatureValue = cfdiSatService.getSignatureValue(privateKey, digesValue, messageSV);

			// verifica llaves correspondientes
			if (!cfdiSatService.verify(messageSV, signatureValue, cer)) {
				throw new RuntimeException("la llave privada y el certificado no corresponden");
			}
			// get message authenticacion
			messageXml = messagesSat.getMessageSolicitud(fechaInicial, fechaFinal, rfcEmisor, tipoSolicitud, encodeCer,
					datosCertificado, numCertificado, digesValue, signatureValue);

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Error al momento de ejecución ");
		}

		return messageXml;
	}

	@Override
	public String getverificacion(String rfcEmisor,String idSolicitud, byte[] cerByte, byte[] keyByte, String password) {
		String encodeCer = null;
		Principal datosCertificado = null;
		BigInteger numCertificado = null;
		String messageXml = null;
		try {

			// encode cer
			encodeCer = Base64.getEncoder().encodeToString(cerByte);
			// get certficado
			X509Certificate cer = cfdiSatService.getCertificate(cerByte);
			int pathLen = cer.getBasicConstraints();
			if (pathLen != -1) {
				LOG.error("El certificado no es un CSD, posee el atributo de Autoridad Certificadora (CA)");
				throw new RuntimeException(
						"El certificado no es un CSD, posee el atributo de Autoridad Certificadora (CA)");
			}
			numCertificado = cer.getSerialNumber();
			datosCertificado = cer.getIssuerDN();

			// get messageDigesValue
			String messageDV = messagesSat.getMessageDigestValueVerificacion(idSolicitud, rfcEmisor);
			// get digesValue
			String digesValue = cfdiSatService.getDigesValue(messageDV);
			// get messageSignatureValue
			String messageSV = messagesSat.getMessageSignatureValueVerificacion(digesValue);
			// get privateKey
			PrivateKey privateKey = cfdiSatService.getPrivateKey(keyByte, password);
			String signatureValue = null;
			// get signatureValue
			signatureValue = cfdiSatService.getSignatureValue(privateKey, digesValue, messageSV);

			// verifica llaves correspondientes
			if (!cfdiSatService.verify(messageSV, signatureValue, cer)) {
				throw new RuntimeException("la llave privada y el certificado no corresponden");
			}
			// get message authenticacion
			messageXml = messagesSat.getMessageVerificacion(idSolicitud, rfcEmisor, encodeCer, datosCertificado,
					numCertificado, digesValue, signatureValue);

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Error al momento de ejecución");
		}

		return messageXml;
	}
	
	@Override
	public String getDescarga(String rfcEmisor,String idPaquete,byte[] cerByte,byte[] keyByte,String password) {
		String encodeCer = null;
		Principal datosCertificado = null;
		BigInteger numCertificado = null;
		String messageXml = null;
		try {

			// encode cer
			encodeCer = Base64.getEncoder().encodeToString(cerByte);
			// get certficado
			X509Certificate cer = cfdiSatService.getCertificate(cerByte);
			int pathLen = cer.getBasicConstraints();
			if (pathLen != -1) {
				LOG.error("El certificado no es un CSD, posee el atributo de Autoridad Certificadora (CA)");
				throw new RuntimeException(
						"El certificado no es un CSD, posee el atributo de Autoridad Certificadora (CA)");
			}
			numCertificado = cer.getSerialNumber();
			datosCertificado = cer.getIssuerDN();

			// get messageDigesValue
			String messageDV = messagesSat.getDigestValueDescarga(idPaquete, rfcEmisor);
			// get digesValue
			String digesValue = cfdiSatService.getDigesValue(messageDV);
			// get messageSignatureValue
			String messageSV = messagesSat.getSignatureValueDescarga(digesValue);
			// get privateKey
			PrivateKey privateKey = cfdiSatService.getPrivateKey(keyByte, password);
			String signatureValue = null;
			// get signatureValue
			signatureValue = cfdiSatService.getSignatureValue(privateKey, digesValue, messageSV);

			// verifica llaves correspondientes
			if (!cfdiSatService.verify(messageSV, signatureValue, cer)) {
				throw new RuntimeException("la llave privada y el certificado no corresponden");
			}
			// get message descarga
			messageXml = messagesSat.getMessageDescarga(idPaquete, rfcEmisor, encodeCer, datosCertificado, numCertificado, digesValue, signatureValue);

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Error al momento de ejecución");
		}

		return messageXml;
	}

}