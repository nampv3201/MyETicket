package com.datn.ticket;

import com.google.gson.Gson;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class TicketApplicationTests {

	@Test
	public void contextLoads() {
		List<Integer> a = new ArrayList<>();
		a.add(1);
		a.add(2);
		System.out.println(a.toString());
	}

}
