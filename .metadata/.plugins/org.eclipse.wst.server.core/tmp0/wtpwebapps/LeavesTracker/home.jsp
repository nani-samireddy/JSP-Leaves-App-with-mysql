<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"  %>
    
<!DOCTYPE html>
		<html>

		<head>
			<meta charset="UTF-8">
			<title>${title}</title>
			<link rel="preconnect" href="https://fonts.googleapis.com">
			<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
			<link
				href="https://fonts.googleapis.com/css2?family=Unbounded:wght@200;300;400;500;600;700;800;900&display=swap"
				rel="stylesheet">
			<style>
				* {
					margin: 0;
					padding: 0;
				}
				body{
				
					font-family: 'Unbounded', cursive;
				}

				header {
					text-align: left;
					padding: 20px 20px;
					background-color: #4BA6FF;
					
				}

				.inputContainer {
					display: flex;
					flex-direction: row;
					gap: 20px;
					justify-content: center;
					align-items: center;
					margin-top:30px;
					font-size:20px;
				}
				.table-container{
					margin-top: 20px;
					display: flex;
					flex-direction: row;
					gap: 20px;
					justify-content: space-evenly;
					align-items: center;
				}
				.leaves-container{
				padding-top: 30px;
				}
				.addleave-container{
				margin-top:30px;
				}
				table,td,th{
					border: 1px solid black;
				}
				
				th{
					padding 40px;
					background-color: grey;
				}
				
				td{
					 padding: 20px;
				    font-size: 1.2rem;
				    text-align: left;
				    border-bottom: 1px solid #ddd;
				}
				
				input{
				padding: 8px 16px;
				border-radius:10px;
				}
				select{
				padding: 8px 32px;
				border-radius: 10px;
				}
			</style>
		</head>

		<body>
			<header>
				<h3>BSCS LEGACY LEAVE TRACKER</h3>
			</header>
			
				<form method="post" action="home">
				<div class="inputContainer">
					Select Employee: <select name="selectEmp" id="selectEmp">
						<c:forEach items="${empsDetails}" var="emp">
							<option  value="${emp.name}">${emp.signum}</option>
						</c:forEach>
					</select>
					<div class="buttonsContainer">
						<input type="submit" name="selectEmp" value="View Employee Details" />
					</div>
			</div>
				</form>
				
				<c:if test="${empLeaves !=null}">
				
				<div class="table-container">
					<div class="leaves-container">
					
						<h4>Employee Leaves </h4> <br>
						<h3>${sname}(${ssignum})</h3> <br>
						<table>
							<tr>
								<td> <b>Date</b> </td>
								<td><b>Type</b></td>
							</tr>
						<c:forEach items="${empLeaves}" var="leave">
							<tr>
								<td>${leave.date}</td>
								<td>${leave.type}</td>
							</tr>
						</c:forEach>
							
						</table>				
					</div>
				
					<div class="addleave-container">
						<form>
							Leave Date: <input type="date" name="date" id="date" /> <br><br>
							Leave Type: <select name="type" id="leaveType">
								<option value="FULL">FULL</option>
								<option value="HALF">HALF</option>
							</select><br><br>
							<input type="hidden" name="signum" value="${ssignum}"/>
							<input type="hidden" name="name" value="${sname}"/>
							<input type="submit" value="Add Leave"/>
						</form>
					</div>
				</div>
					
				
				
				</c:if>
		</body>

		</html>
