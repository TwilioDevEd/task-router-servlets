<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="x-ua-compatible" content="ie=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TaskRouter for Servlets</title>
    <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
      <script src="https://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
    <link href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css" rel="stylesheet" integrity="sha256-MfvZlkHCEqatNoGiOXveE8FIwMzZg4W85qfrfIFBfYc= sha512-dTfge/zgoMYpP7QbHy4gWMEGsbsdZeCXz7irItjcC3sPUFtf0kuFbDz/ixG7ArTxmDjLXDmezHubeNikyKGVyQ==" crossorigin="anonymous">
    <link href="https://maxcdn.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.min.css" rel="stylesheet" integrity="sha256-k2/8zcNbxVIh5mnQ52A0r3a6jAgMGxFJFE2707UxGCk= sha512-ZV9KawG2Legkwp3nAlxLIVFudTauWuBpC10uEafMHYL0Sarrz5A7G79kXh5+5+woxQ5HM559XX2UZjMJ36Wplg==" crossorigin="anonymous">
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
                <!-- Table -->
                <table class="table">
                  <tbody>
                    <c:forEach var="missed_call" items="${missed_calls}" varStatus="i">
                       <c:set var="jobID" value="${jobs.jobId}"/>
                        <tr>
                            <td>${missed_call.selectedProduct}</td>
                            <td><a href="tel:${missed_call.phoneNumber}">${missed_call.internationalPhoneNumber}</a></td>
                        </tr>
                    </c:forEach>
                  </tbody>
                </table>
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
