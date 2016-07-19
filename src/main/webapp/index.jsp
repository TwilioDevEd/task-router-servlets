<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="x-ua-compatible" content="ie=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Task Router for Servlets</title>
    <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
      <script src="https://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
    <link href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css" rel="stylesheet" crossorigin="anonymous">
    <link href="https://maxcdn.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.min.css" rel="stylesheet" crossorigin="anonymous">
    <link rel="stylesheet" href="css/task-router.css">
</head>
<body>
<div class="container">
    <section class="page-header">
        <h1>Missed Calls</h1>
    </section>
    <section class="body-content">
          <div class="panel panel-default full-height-container">
                <div class="panel-heading"><strong>Missed calls</strong> <span class="text-muted">Product/Number<span></div>
                  <c:choose>
                      <c:when test="${missedCalls.isEmpty()}">
                        <div class="panel-body">
                          <p>There are no missed calls at the moment.</p>
                          <p>Call to your Twilio Phone number:<p>
                          <ul>
                            <c:set var="phone" value="${settings.phoneNumber}"/>
                             <li><a href="tel:${phone.phoneNumber}">${phone.internationalPhoneNumber}</a></li>
                          </ul>
                        </div>
                      </c:when>
                      <c:otherwise>
                        <!-- Table -->
                        <table class="table">
                          <tbody>
                            <c:forEach var="missedCall" items="${missedCalls}" varStatus="i">
                              <tr>
                                <td>${missedCall.selectedProduct}</td>
                                <td><a href="tel:${missedCall.phoneNumber}">${missedCall.internationalPhoneNumber}</a></td>
                              </tr>
                            </c:forEach>
                          </tbody>
                        </table>
                      </c:otherwise>
                  </c:choose>
           </div>
    </section>
</div>
<footer class="footer">
    <div class="container">
        <p class="text-muted">
            Made with <i class="fa fa-heart"></i> by your pals
            <a href="http://www.twilio.com">@twilio</a>
        </p>
    </div>
</footer>
</body>
</html>
