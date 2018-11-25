package com.example.jnucecodefestival.compileservice;

import static org.mockito.ArgumentMatchers.booleanThat;

import java.util.Map;

import com.example.jnucecodefestival.model.CompileRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.example.jnucecodefestival.compileservice.grade.*;



@Controller
public class CompileController {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @GetMapping(
        value="/compile"
    )
    @ResponseBody
    public String getCompile(@RequestParam String code) {
        return "안돼";
    }

    @PostMapping(
        value="/compile",
        produces= "application/json"
    )
    @ResponseBody
    public String postCompile(@RequestBody CompileRequest code) throws Exception{
        Grade userGrade = null;
        
        String getGradeQuery = 
        "select grade from users where username=\'" + code.getCreateAuthor() + "\'";
        int grade = Integer.parseInt(jdbcTemplate.queryForMap(getGradeQuery).get("grade").toString());
        

        System.out.println(grade + "학년입니다.");
        switch(grade) {
            case 1:
                userGrade = new FirstGrade();
                break;
            case 2:
                userGrade = new SecondGrade();
                break;
            case 3:
                userGrade = new ThirdGrade();
                break;
            default:
                break;
        }
        Object result = Compile.compile(code.getLanguage(), code.getCreateAuthor(), code.getCode(), code.getNumber(), grade);

        // try {
        //     String getAnswerQuery = 
        //     "select problemAnswer from problem where grade=(select grade from users where username=\'"
        //      + code.getCreateAuthor() +
        //      "\') and problemNum=\'" + code.getNumber() + "\'";
        //     Map<String, Object> expectedOutput = jdbcTemplate.queryForMap(getAnswerQuery);
        //     System.out.println(result);
        //     System.out.println(expectedOutput.get("problemAnswer") + "입니다.");

        //     if(expectedOutput.get("problemAnswer").toString().trim().equals(result.toString().trim())) {
        //         return "정답!";
        //     } else {
        //         return "틀렸습니다!";
        //     }
        // } catch (DataAccessException e) {
        //     System.out.println("바보");
        //     e.printStackTrace();
        // }


        
        
        // System.out.println(code.toString());
        return result.toString();
    }
}