package com.nani;

import java.io.IOException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/leaves")
public class LeavesServlet {
	public void service(HttpServletRequest req, HttpServletResponse res) throws  IOException, ServletException {
		String title = "no leaves";
			//ResultSet data = new DatabaseService().getEmployeNames();
			
			//String[] eNames = new String[data.getInt("count(*)"+" rows")];
			String[] eNames = {"one","two"};
			req.setAttribute("eNames", eNames);
			req.setAttribute("title", title);

			RequestDispatcher rd = req.getRequestDispatcher("leaves.jsp");
			rd.forward(req, res);
		
		
	}

}
