package com.datn.ticket;

import com.google.gson.Gson;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class TicketApplicationTests {

	@Test
	public void contextLoads() {
		List<Object> test = new ArrayList<>();
		for(int i = 0; i<2; i++) {
			Map<String, Object> td= new HashMap<>();
			td.put("test", "test");
			td.put("number", i);
			test.add(td);
		}

		for(Object obj : test) {
			System.out.println(obj);
		}
	}

}
