<?php
class Orderproc extends CI_Controller {
	function index() {
		$this->load->helper ( 'form' );
		$this->load->helper ( 'html' );
		echo heading ( 'Manager Control Panel', 2 );
		echo form_open ( 'orderproc/showorders' );
		echo form_submit ( 'ShowOrders', 'ShowOrders!' );
		// $this->load->view('management');
	}
	
	/**
	 * Post 方式请求网页数据
	 *
	 * @param string $url
	 *        	网页地址
	 *        	@prarm string $host 主机
	 * @param string $session
	 *        	会话值
	 *        	@prarm string $type 类型(POST、GET)
	 *        	@prarm string $port 端口
	 *        	@prarm string $data 数据
	 */
	function getPageConent($url, $host, $session = "", $type = "GET", $port = "", $data = "") {
		
		// if( empty($port) ) $port = 80;
		if (empty ( $port ))
			$port = 7070;
			
			/* 请求数据 */
		$post_data = $data;
		$lenght = strlen ( $post_data );
		
		$headers = "{$type} {$url} HTTP/1.1\r\n";
		$headers .= "Accept: * /*\r\n";
		$headers .= "Content-Type: application/x-www-form-urlencoded\r\n";
		$headers .= "User-Agent: Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; GTB6; CIBA; .NET CLR 4.0.20506)\r\n";
		if ($session != "")
			$headers .= "Cookie:JSESSIONID={$session}\r\n";
		$headers .= "Host: {$host}:{$port}\r\n";
		$headers .= "Content-Length: {$lenght}\r\n";
		$headers .= "Connection: Close\r\n\r\n";
		$headers .= $post_data;
		
		if ($fp = fsockopen ( $host, $port, $errno, $errstr, 100 )) {
			fwrite ( $fp, $headers );
			$header = fread ( $fp, 1024 );
			$content = fread ( $fp, 1024 );
			$content .= fread ( $fp, 1024 );
			$content .= fread ( $fp, 1024 );
			$content .= fread ( $fp, 1024 );
			fclose ( $fp );
		}
		if ($data != "") {
			echo $headers;
			echo "<hr />";
			echo $header;
			echo "<hr />";
			echo $content;
			echo "<hr />";
			exit ();
		} else {
			return $content;
		}
	}
	
	// function sendNotificationTest() {
	// $URL =
	// '/notification.do?action=send&user=10.0.2.15.abcrestaurant.com&title=private&message=wangfeitest&uri=null';
	// $HOST = 'localhost';
	// echo 'URL:'.$URL.' HOST:'.$HOST;
	// $this->getPageConent($URL, $HOST);
	// }
	
	// function sendNotificationTest1() {
	// $URL =
	// '/notification.do?action=send&user=10.0.2.15.abcrestaurant.com&title=private&message=wangfeitest&uri=null';
	// $HOST = 'localhost';
	// $this->sendTargetNotification('10.0.2.15', 'PHP', "PHP");
	// }
	
	function sendNotification($broadcast, $user, $title, $message, $uri) {
		$this->load->helper ( 'url' );
		$user_suffix = '.abcrestaurant.com';
		// $URL = 'http://localhost:7070/notification.do?action=send';
		$URL = '/notification.do?action=send';
		if ($broadcast == TRUE) {
			$URL .= '&broadcast=Y';
		} else {
			$URL .= '&user=' . $user . $user_suffix;
		}
		$URL .= '&title=' . $title;
		$URL .= '&message=' . $message;
		
		$URL .= '&uri=' . $uri;
		$HOST = $this->getAndroidPnServer();
		echo 'URL:' . $URL . ' HOST:' . $HOST;
		$this->getPageConent ( $URL, $HOST );
	}
	
	/* retrieve the Andorid Push Notification Server host name */
	function getAndroidPnServer() {
		return "localhost";
	}
	
	function sendTargetNotification($user, $title, $message, $uri = 'null') {
		$this->sendNotification ( FALSE, $user, $title, $message, $uri );
	}
	
	function sendBroadcastNotification($title, $message, $uri = 'null') {
		$this->sendNotification ( TRUE, 'null', $title, $message, $uri );
	}
	
	function saveclientorder() {
		// ob_start();
		
		if (! isset ( $_POST ['json'] )) {
			echo "Save Order Error!";
			return;
		}
		
		$orderObj = json_decode ( stripslashes ( $_POST ['json'] ), true );
		
		$this->load->database ();
		// print_r($orderObj);
		// echo "The order_id is is $orderObj[id]";
		// echo "The order_ip is is "; echo $this->input->ip_address();
		// echo "The order_time is is "; echo $orderObj['time'];
		
		// Insert the order to DB.
		/* Note:no need to let client tell us the order id */
		$order_data = array (
				// 'id' => $orderObj['id'],
				'ip' => $orderObj ['ip'],
				'time' => $orderObj ['time'],
				'cost' => $orderObj ['totalprice'] 
		);
		// print_r ( $order_data );
		$this->db->insert ( 'orders', $order_data );
		
		$this->db->select_max ( 'id' );
		$query = $this->db->get ( 'orders' );
		$cur_order_id = 1;
		foreach ( $query->result () as $row ) {
			$cur_order_id = $row->id;
		}
		
		// print_r(date('Y-m-d H:i:s', $orderObj['time'] / 1000));
		
		// Insert orderred course to DB.
		foreach ( $orderObj ['courses'] as $course ) {
			$orderred_course_data = array (
					'course_id' => $course ['id'],
					'num' => $course ['num'],
					// 'order_id' => $orderObj['id']
					'order_id' => $cur_order_id 
			);
			// print_r($orderred_course_data);
			$this->db->insert ( 'orderred_courses', $orderred_course_data );
			
			/*
			 * besides insert the order & orderred_courses into their tables we
			 * increase the order_count field of abc_courses by 1
			 */
			$courseid = $course ['id'];
			$this->db->where ( 'id', $courseid );
			$this->db->set ( 'order_count', 'order_count + 1', FALSE );
			$this->db->update ( 'courses' );
		}
		
		/*
		 * order information is saved to Mysql Tables
		 */
		$order_id = $cur_order_id;
		
		$client_user_name = $orderObj ['ip'];
		// $order_id = $orderObj['id'];
		$title = '订单(编号:' . $order_id . ')已经受理';
		//$message = '亲，您的菜品正在烹饪中，请耐心等待:)';
		$message = $title;
		$this->sendTargetNotification ( $client_user_name, $title, $message );
		$this->dispatchOrderredCourseToCooker();
		// dumpinfo
		// $page = ob_get_contents();
		// ob_end_flush();
		// $fp = fopen("/home/wangfei/phpoutput/phplog.txt","w");
		// fwrite($fp,$page);
		// fclose($fp);
	}
	
	function getCourses() {
		$this->load->database ();
		$this->load->helper ( 'html' );
		$this->load->library ( 'table' );
		
		$orders = $this->db->select ( 'name, img' );
		$courses = $this->db->from ( 'courses' );
		
		$this->db->order_by ( "id", "asc" );
		$query = $this->db->get ();
		// $this->load->view('showorders', $data);
		foreach ( $query as &$res ) {
			$this->output->set_content_type ( 'png' );
			$this->output->set_output ( $res ['img'] );
		}
	}
	
	function getNameByID($courseID) {
		$this->load->database ();
		$this->db->where('id', $courseID);
		$query = $this->db->get ( 'courses' );
		foreach ($query->result() as $course) {
			return $course->name;
		}
	}
	
	function alterOrderredCourseStatus($orderID, $courseID, $transitionMode) {
		$this->load->database ();
		
		/* 0->pending yet; 1->cooking; 2->finished */
		/* firstly, update the status of a orderred course */
		$this->db->where ( 'course_id', $courseID );
		$this->db->where ( 'order_id', $orderID );
		if (1 == $transitionMode) {
			$this->db->set ( 'deliverstatus', 1);
		}
		elseif (2 == $transitionMode) {
			$this->db->set ( 'deliverstatus', 2);
		} else {
			return;
		}
		$this->db->update ( 'orderred_courses' );
		/*TODO check the whole order is totally delivered */
		
		/* secondly, post the delivery info to the customer */
		$this->db->where ( 'id', $orderID );
		$queryClientIP = $this->db->get ( 'orders' );
		$ClientIP;
		foreach ($queryClientIP->result() as $curOrder) {
			$ClientIP = $curOrder->ip;
			break;
		}
		
		if (empty($ClientIP)) {
			return;
		}
		
		$CourseName = $this->getNameByID($courseID);
		if (2 == $transitionMode) {
			$title = '美味*'.$CourseName.'*已经烹饪完成';
			$message = '亲，您的菜:'.$CourseName.'已经完成，请准备好食欲:-)';
			$this->sendTargetNotification ( $ClientIP, $title, $message );
		}
		elseif (1 == $transitionMode) {
			$title = '美味*'.$CourseName.'*已经开始烹饪';
			$message = '亲，您的菜:'.$CourseName.'正由厨师准备中，请耐心等候:-)';
			$this->sendTargetNotification ( $ClientIP, $title, $message );		
		}
		
		/*thirdly , update the status of the corresponding cooker who is response for the course */
		if (2 == $transitionMode) {
			$this->db->where ( 'order_id', $orderID );
			$this->db->where ( 'course_id', $courseID );
			$queryCorCooker = $this->db->get ( 'orderred_courses' );
			/* the ID of the cooker whom to be released out */
			$nextAvailableCookerID;
			foreach ($queryCorCooker->result() as $curCouse) {
				$nextAvailableCookerID = $curCouse->cooker_id;
				break;
			}
			
			if (empty($nextAvailableCookerID)) {
				return;
			}
			
			$this->db->where ( 'id', $nextAvailableCookerID);
			$this->db->set ( 'status', 0);
			$this->db->update ( 'cookers' );
		}
	}
	
	function dispatchOrderredCourseToCookerTest() {
		$this->load->database ();
		$this->load->helper ( 'html' );
		$this->load->library ( 'table' );
		echo meta( 'Content-type', 'text/html; charset=utf-8', 'equiv' );
		$AVAILABLE = 0;
		$this->db->where ( 'status =', $AVAILABLE );
		$query = $this->db->get ( 'cookers' );
		echo $this->table->generate ( $query );
	}
	
	/* dispatch the orderred courses to available cookers */
	function dispatchOrderredCourseToCooker() {
		/* first find out the orderred_courses that not deliverred */
		$this->load->database ();
		$this->load->library ( 'table' );
		// $this->db->select('id');
		$NOT_DELIVERRED = 0;
		$this->db->where ( 'deliverstatus =', $NOT_DELIVERRED );
		$querycourse = $this->db->get ( 'orderred_courses' );
		foreach ( $querycourse->result () as $course_cur_processing ) {
			/*store the id of the current course processing*/
			$course_id = $course_cur_processing->course_id;
			$order_id = $course_cur_processing->order_id;
			/* second find out the first available cooker */
			$AVAILABLE = 0;
			$this->db->where ( 'status =', $AVAILABLE );
			$querycooker = $this->db->get ( 'cookers' );
			
			foreach ($querycooker->result() as $available_cooker) {
				/*dispatch this course to the first available cooker*/
				$this->db->where ( 'course_id', $course_id );
				$this->db->where ( 'order_id', $order_id );
				$this->db->set ( 'cooker_id', $available_cooker->id);
				$this->db->update ( 'orderred_courses' );
				/*after dispatching, update the status of the cooker to BUSY*/
				$this->db->where ( 'id', $available_cooker->id );
				$BUSY = 1;
				$this->db->set ( 'status', $BUSY);
				$this->db->update ( 'cookers' );
				
				/*lastly, send an notification to the cooker panel*/
				$cooker_client_ip = $available_cooker->ip;
				/* populate both course_id & order_id into one bundle */
				$course_id = $course_id .".".$order_id;
// 				$this->sendTargetNotification ( "10.0.2.2", "NEWCOURSEFORCOOKER", $course_id);
				$this->sendTargetNotification ( $cooker_client_ip, "NEWCOURSEFORCOOKER", $course_id);
				break;
			}
		}
	}
	
	function showorders() {
		$this->load->database ();
		$this->load->helper ( 'html' );
		$this->load->library ( 'table' );
		
		// first get orders
		$this->load->database ();
		$this->db->select ( 'id, time, cost' );
		$this->db->order_by ( "id", "asc" );
		$query_order = $this->db->get ( 'orders' );
		echo meta ( 'Content-type', 'text/html; charset=utf-8', 'equiv' );
		foreach ( $query_order->result_array () as $order ) {
			// echo var_dump($row);
			// array_push($courselist,$order);
			$cur_order_id = $order ['id'];
			$this->db->select ( 'orderred_courses.order_id as 订单号, courses.name as 菜品, ip as 客户端IP, time as 提交时间, orderred_courses.num as 数量, courses.price as 单价（元）' );
			$this->db->from ( 'orderred_courses' );
			$this->db->where ( 'orderred_courses.order_id', $cur_order_id );
			$this->db->join ( 'orders', 'orders.id = orderred_courses.order_id' );
			$this->db->join ( 'courses', 'courses.id = orderred_courses.course_id' );
			// $this->db->group_by('ORDER_ID');
			
			// $this->db->order_by("order_id", "asc");
			// $this->db->order_by("course_id", "asc");
			$query = $this->db->get ();
			// $this->load->view('showorders', $data);
			
			$tmpl = array (
					'table_open' => '<table border="4" cellpadding="4" cellspacing="0">',
					'heading_row_start' => '<tr>',
					'heading_row_end' => '</tr>',
					'heading_cell_start' => '<th>',
					'heading_cell_end' => '</th>',
					
					'row_start' => '<tr>',
					'row_end' => '</tr>',
					'cell_start' => '<td>',
					'cell_end' => '</td>',
					
					'row_alt_start' => '<tr>',
					'row_alt_end' => '</tr>',
					'cell_alt_start' => '<td>',
					'cell_alt_end' => '</td>',
					
					'table_close' => '</table>' 
			);
			
			$this->table->set_template ( $tmpl );
			$heading = "订单号:" . $order ['id'] . "   下单时间:" . $order ['time'] . "   订单金额:" . $order ['cost'] . "元";
			$this->table->set_caption ( $heading );
			echo $this->table->generate ( $query );
			echo br ();
		}
	
	}
}
?>
