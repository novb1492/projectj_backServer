package com.kimcompay.projectjb.users.user.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Data
@ToString
public class tryInsertDto {
    
    @NotBlank(message = "이메일이 빈칸입니다")
    @Email(message = "이메일 형식으로 적어주세요")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요")
    @Size(min = 4,max = 10,message = "비밀번호는 최소 4 최대 10자리 입니다")
    private String pwd;

    @NotBlank(message = "비밀번호를 입력해주세요")
    @Size(min = 4,max = 10,message = "비밀번호는 최소 4 최대 10자리 입니다")
    private String pwd2;

    @NotBlank(message = "우편번호가 빈칸입니다")
    private String post_code;

    @NotBlank(message = "주소가 빈칸입니다")
    private String address;

    @NotBlank(message = "상세주소가 빈칸입니다")
    private String detail_address;

    @NotBlank(message = "휴대폰번호가 빈칸입니다")
    private String phone;
    //회원유형
    @NotBlank(message = "회원유형이 없습니다")
    private String scope;
    @NotBlank(message = "이름을 입력해주세요")
    private String name;
    //일반인일때만 따로검사
    //나이스 본인인증만 할 수있어도 필요없는검사로 보임
    private String birth;
    //기업시에만 따로 검사
    private String tel;
    private String company_num;
    private String store_name;
    private String start_dt;
    //db에는 숫자로 회원유형이 들어감
    private int scope_num;


}
