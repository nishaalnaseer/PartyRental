-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               10.11.2-MariaDB - mariadb.org binary distribution
-- Server OS:                    Win64
-- HeidiSQL Version:             11.3.0.6295
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Dumping database structure for party_rental
CREATE DATABASE IF NOT EXISTS `party_rental` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */;
USE `party_rental`;

-- Dumping structure for table party_rental.customer
CREATE TABLE IF NOT EXISTS `customer` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `password` varchar(70) NOT NULL,
  `email` varchar(50) NOT NULL,
  `type` int(11) NOT NULL,
  `status` enum('REQUESTED','ENABLED','DISABLED') NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `id` (`id`) USING BTREE,
  UNIQUE KEY `email` (`email`) USING BTREE,
  KEY `FK_customer_customer_type` (`type`),
  CONSTRAINT `FK_customer_customer_type` FOREIGN KEY (`type`) REFERENCES `customer_type` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Data exporting was unselected.

-- Dumping structure for table party_rental.customer_type
CREATE TABLE IF NOT EXISTS `customer_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `label` varchar(20) NOT NULL,
  `tax_description` varchar(5) NOT NULL,
  `tax` float NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `label` (`label`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Data exporting was unselected.

-- Dumping structure for table party_rental.employee
CREATE TABLE IF NOT EXISTS `employee` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `password` varchar(70) NOT NULL,
  `email` varchar(50) NOT NULL,
  `role` enum('ADMINISTRATOR','OFFICER') NOT NULL,
  `status` enum('REQUESTED','ENABLED','DISABLED') NOT NULL DEFAULT 'REQUESTED',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Data exporting was unselected.

-- Dumping structure for table party_rental.item
CREATE TABLE IF NOT EXISTS `item` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `description` varchar(100) NOT NULL,
  `rate` float NOT NULL,
  `created_by` int(11) NOT NULL,
  `created_on` date NOT NULL,
  `stock` int(11) NOT NULL,
  `available` int(11) NOT NULL,
  `reserved` int(11) NOT NULL,
  `rented` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `description` (`description`),
  UNIQUE KEY `id` (`id`) USING BTREE,
  KEY `FK_item_employee` (`created_by`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Data exporting was unselected.

-- Dumping structure for table party_rental.items
CREATE TABLE IF NOT EXISTS `items` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `reservation_id` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  `qty` int(11) NOT NULL,
  `price` float NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `unique_reservations_items` (`reservation_id`,`item_id`),
  KEY `FK_items_item` (`item_id`),
  CONSTRAINT `FK_items_item` FOREIGN KEY (`item_id`) REFERENCES `item` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_items_reservation` FOREIGN KEY (`reservation_id`) REFERENCES `reservation` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Data exporting was unselected.

-- Dumping structure for table party_rental.requested_customer
CREATE TABLE IF NOT EXISTS `requested_customer` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `password` varchar(70) NOT NULL,
  `email` varchar(50) NOT NULL,
  `type` int(11) NOT NULL DEFAULT 0,
  `status` enum('REQUESTED','ENABLED','DISABLED') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `FK_requested_customer_customer_type` (`type`),
  CONSTRAINT `FK_requested_customer_customer_type` FOREIGN KEY (`type`) REFERENCES `customer_type` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Data exporting was unselected.

-- Dumping structure for table party_rental.reservation
CREATE TABLE IF NOT EXISTS `reservation` (
  `id` int(11) NOT NULL,
  `customer` int(11) NOT NULL,
  `remarks` varchar(150) NOT NULL DEFAULT '',
  `approved_by` int(11) DEFAULT NULL,
  `rented_by` int(11) DEFAULT NULL,
  `return_accepted_by` int(11) DEFAULT NULL,
  `reservation_date` date NOT NULL,
  `rent_date` date NOT NULL,
  `return_date` date NOT NULL,
  `status` enum('REQUESTED','DENIED','RESERVATION_CANCELLED','RESERVED','LEASING_CANCELLED','RENTED','RETURNED') DEFAULT 'REQUESTED',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `FK_reservation_customer` (`customer`),
  KEY `FK_reservation_approved_by` (`approved_by`),
  KEY `FK_reservation_rented_by` (`rented_by`),
  KEY `FK_reservation_return_accepted_by` (`return_accepted_by`),
  CONSTRAINT `FK_reservation_approved_by` FOREIGN KEY (`approved_by`) REFERENCES `employee` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_reservation_customer` FOREIGN KEY (`customer`) REFERENCES `customer` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_reservation_rented_by` FOREIGN KEY (`rented_by`) REFERENCES `employee` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_reservation_return_accepted_by` FOREIGN KEY (`return_accepted_by`) REFERENCES `employee` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Data exporting was unselected.

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
