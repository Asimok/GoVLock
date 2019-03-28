package com.example.gov_lock.facelogin.HttpSerever;

public class FaceMessage {
private String Name;

	public String getEmployeeNumber() {
		return EmployeeNumber;
	}

	public void setEmployeeNumber(String employeeNumber) {
		EmployeeNumber = employeeNumber;
	}

	private String EmployeeNumber;
	public FaceMessage(byte[] faceFeature, String name,String employeeNumber) {
		this.FaceFeature = faceFeature;
		this.Name = name;
		this.EmployeeNumber=employeeNumber;
	}
	public byte[] getFaceFeature() {
		return FaceFeature;
	}

	public void setFaceFeature(byte[] faceFeature) {
		FaceFeature = faceFeature;
	}

	private byte[] FaceFeature;

public String getName() {
	return Name;
}
public void setName(String name) {
	Name = name;
}

}
