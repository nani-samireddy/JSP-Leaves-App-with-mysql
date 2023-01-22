<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    <%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
    
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

				body {
					background-color: #D7FBE6;
				}

				header {
					text-align: center;
					padding: 50px 0px;
					font-family: 'Unbounded', cursive;
				}

				.inputContainer {
					display: flex;
					justify-content: center;
					align-items: center;
				}

			</style>
		</head>

		<body>
			<header>
				<h1>BSCS LEGACY LEAVE TRACKER</h1>
			</header>
			<div class="inputContainer">
				<form>
					Select Employee: <select name="EmployeeNames" id="EmployeeNames">
						
					</select>
					<br><br>
					<div class="buttonsContainer">
						<input type="submit" value="View Leave Details" />
						<button>Add Leave</button>
					</div>
				</form>

			</div>
		</body>

		</html>
