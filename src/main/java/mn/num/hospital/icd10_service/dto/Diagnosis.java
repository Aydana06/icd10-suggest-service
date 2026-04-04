package mn.num.hospital.icd10_service.dto;

//Data class (backend-с ирэх бүтэц)
public class Diagnosis {
 public String code;
 public String name;
 public String detail;

 public Diagnosis(String code, String name, String detail) {
     this.code = code;
     this.name = name;
     this.detail = detail;
 }

}
