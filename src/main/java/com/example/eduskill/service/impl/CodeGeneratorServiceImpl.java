package com.example.eduskill.service.impl;

import com.example.eduskill.entity.CodeSequence;
import com.example.eduskill.repository.CodeSequenceRepository;
import com.example.eduskill.service.CodeGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CodeGeneratorServiceImpl implements CodeGeneratorService {

    private final CodeSequenceRepository repo;


    @Override
    public String generateStudentCode(){

        CodeSequence seq = repo.findByPrefix("STD")
                .orElseGet(() -> {
                    CodeSequence s = new CodeSequence();
                    s.setPrefix("STD");
                    s.setSeq(0L);
                    return repo.save(s);
                });

        seq.setSeq(seq.getSeq()+1);
        repo.save(seq);

        return "STD-"+String.format("%05d",seq.getSeq());

    }
    @Override
    public String generateEmployeeCode(String rolePrefix){

        int year = LocalDate.now().getYear() % 100; // 2026 -> 26

        CodeSequence seq = repo.findByPrefix(rolePrefix)
                .orElseGet(() -> {
                    CodeSequence s = new CodeSequence();
                    s.setPrefix(rolePrefix);
                    s.setSeq(0L);
                    return repo.save(s);
                });

        seq.setSeq(seq.getSeq()+1);
        repo.save(seq);

        String sequence = String.format("%06d", seq.getSeq());

        return year + rolePrefix  + sequence;
    }

}