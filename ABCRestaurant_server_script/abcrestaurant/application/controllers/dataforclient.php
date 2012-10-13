<?php
/**
 * Class Dataforclient - controller which used to return information to clients(ABCRestaurant mobile application)
 */
class Dataforclient extends CI_Controller {

    function __construct() {
        parent::__construct();
		$this->load->helper ( 'form' );
		$this->load->helper ( 'html' );
		$this->load->database ();
		$this->load->library ( 'table' );
    }

	function index() {
		echo heading ( 'Manager Control Panel', 2 );
		echo form_open ( 'orderproc/showorders' );
		echo form_submit ( 'ShowOrders', 'ShowOrders!' );
		// $this->load->view('management');
	}

	function getCategoryNo() {
		$orders = $this->db->select ( 'id' );
		$courses = $this->db->from ( 'abc_category' );
		return $this->db->count_all_results ();
	}

	/**
	 * retrieve metadata of courses info
	 */
	function getCourseNo() {
		$orders = $this->db->select ( 'id' );
		$courses = $this->db->from ( 'abc_courses' );
		return $this->db->count_all_results ();
	}

	function getCourses() {
		$this->db->select ( 'id, name, category_id, price, order_count' );
		$this->db->where ( 'available', 1);
		$this->db->order_by ( "id", "asc" );
		$query = $this->db->get ( 'abc_courses' );
		$courselist = array ();
		foreach ( $query->result_array () as $row ) {
			// echo var_dump($row);
			array_push ( $courselist, $row );
		}
		/* Test code */
		/*
		$data['course'] = $courselist;
		$this->load->view('management', $data);
		*/
		return $courselist;
	}

	function getCategories() {
		$this->db->select ( 'id, name' );
		$this->db->order_by ( "id", "asc" );
		$query = $this->db->get ( 'abc_category' );
		$categorylist = array ();
		foreach ( $query->result_array () as $row ) {
			// echo var_dump($row);
			array_push ( $categorylist, $row );
		}
		/* Test code */
		/*
	    $data['course'] = $categorylist;
		$this->load->view('management', $data);
		*/
		return $categorylist;
	}

	function retrieveCourseImgById($courseID) {
		$this->load->database ();
		$orders = $this->db->select ( 'img as IMG' );
		$courses = $this->db->from ( 'abc_courses' );
		$this->db->where ( 'id', $courseID );
		$this->db->where ( 'available', 1);
		$query = $this->db->get ();
		foreach ( $query->result () as $row ) {
			$this->output->set_content_type ( 'png' );
			$this->output->set_output ( $row->IMG );
		}
	}

	function retrieveCategoryImgById($courseID) {
		$this->load->database ();
		$orders = $this->db->select ( 'img as IMG' );
		$courses = $this->db->from ( 'abc_category' );
		$this->db->where ( 'id', $courseID );
		$query = $this->db->get ();
		foreach ( $query->result () as $row ) {
			$this->output->set_content_type ( 'png' );
			$this->output->set_output ( $row->IMG );
		}
	}

	function getMetadata() {
	    /**
	    NOTE!!! in php version larger than 5.2.0, the json helper is no longer needed, as
	    the function json_encode & json_decode is internally included in PHP
	    */
		//$this->load->helper ( 'json' );
		$metadata ['courseNo'] = $this->getCourseNo ();
		$metadata ['cateNo'] = $this->getCategoryNo ();
		$metadata ['courses'] = $this->getCourses ();
		$metadata ['categories'] = $this->getCategories ();
		// $baseurl = $this->config->site_url();
		/* Note !!! the url should be paid more attention */
		$baseurl = "http://192.168.1.133/abcrestaurant/index.php";
		$urlinfo ['courseImgURL'] = "$baseurl/dataforclient/retrieveCourseImgById";
		$urlinfo ['categoryImgURL'] = "$baseurl/dataforclient/retrieveCategoryImgById";
		$metadata ['urlinfo'] = $urlinfo;
		$jsonoutput ['metadata'] = $metadata;
		$this->output->set_content_type ( 'application/json' )->set_output ( json_encode ( $jsonoutput ) );
	}

	function getCoursesIMG() {
		$this->load->database ();
		$this->load->helper ( 'html' );

		$orders = $this->db->select ( 'id, img' );
		$courses = $this->db->from ( 'abc_courses' );
		$this->db->where ( 'available', 1);
		$this->db->order_by ( "id", "asc" );
		$query = $this->db->get ();
		$baseurl = $this->config->site_url ();
		foreach ( $query->result () as $res ) {
			echo "<IMG SRC=\"$baseurl/dataforclient/retrieveCourseImgById/$res->id\">";
		}
	}

	function getCategoriesIMG() {
		$orders = $this->db->select ( 'id, name, img' );
		$courses = $this->db->from ( 'abc_category' );
		$this->db->order_by ( "id", "asc" );
		$query = $this->db->get ();
		$baseurl = $this->config->site_url ();
		foreach ( $query->result () as $cate_item ) {
			echo "<IMG SRC=\"$baseurl/dataforclient/retrieveCategoryImgById/$cate_item->id\">";
		}
	}

}
?>
