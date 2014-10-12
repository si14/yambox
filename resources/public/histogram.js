google.load("visualization", "1", {packages:["corechart"]});
google.setOnLoadCallback(drawChart);
function drawChart() {
    var data = google.visualization.arrayToDataTable(columns);
    var options = {
        legend: { position: 'none' },
        colors: ['#428bca'],
        chartArea: { left: '10%', top: '10%', width: "80%", height: "80%" },
    };

    var chart = new google.visualization.Histogram(document.getElementById('histogram'));
    chart.draw(data, options);
}