-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Dec 19, 2025 at 10:03 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `utilities_platform`
--

-- --------------------------------------------------------

--
-- Table structure for table `bill`
--

CREATE TABLE `bill` (
  `id` int(11) NOT NULL,
  `Subscriber` varchar(50) NOT NULL,
  `Amount` decimal(10,2) NOT NULL,
  `IssueDate` date NOT NULL,
  `Reference` varchar(100) NOT NULL,
  `Status` enum('Pending','Paid','Overdue') DEFAULT 'Pending',
  `DueDate` date NOT NULL,
  `BillID` varchar(10) DEFAULT NULL,
  `Services` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `bill`
--

INSERT INTO `bill` (`id`, `Subscriber`, `Amount`, `IssueDate`, `Reference`, `Status`, `DueDate`, `BillID`, `Services`) VALUES
(1, '1', 500.00, '2025-10-28', 'ELEC-2024-0021', 'Pending', '2025-11-12', 'B-001', '2'),
(2, '1', 1000.00, '2025-10-28', 'Ref-003', 'Pending', '2025-10-28', 'B-002', '2'),
(6, '1', 1000.00, '2025-10-28', 'Ref-004', 'Pending', '2025-10-28', 'B-003', '2'),
(7, '1', 500.00, '2025-10-28', 'Ref-007', 'Pending', '2025-10-28', 'B-007', '1'),
(14, 'Sub-006', 1200.00, '2025-10-28', 'Ref-013', 'Pending', '2025-10-28', 'B-013', '3'),
(16, 'Sub-001', 500.00, '2025-10-28', 'Ref-101', 'Pending', '2025-10-28', 'B-101', 'Water'),
(17, 'Sub-002', 1200.00, '2025-10-28', 'Ref-102', 'Pending', '2025-10-28', 'B-102', 'Electricity'),
(19, 'Sub-004', 1500.00, '2025-10-28', 'Ref-104', 'Pending', '2025-10-28', 'B-104', 'Internet'),
(20, 'Sub-005', 700.00, '2025-10-28', 'Ref-105', 'Pending', '2025-10-28', 'B-105', 'Water');

--
-- Triggers `bill`
--
DELIMITER $$
CREATE TRIGGER `trg_bill_before_insert` BEFORE INSERT ON `bill` FOR EACH ROW BEGIN
  
  IF NEW.BillID IS NULL OR NEW.BillID = '' THEN
    SET NEW.BillID = CONCAT('B-', LPAD((SELECT IFNULL(MAX(id),0)+1 FROM bill), 3, '0'));
  END IF;

  
  IF NEW.Reference IS NULL OR NEW.Reference = '' THEN
    SET NEW.Reference = CONCAT('Ref-', LPAD((SELECT IFNULL(MAX(id),0)+1 FROM bill), 3, '0'));
  END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `complaint`
--

CREATE TABLE `complaint` (
  `ComplaintID` varchar(10) NOT NULL,
  `Subscriber` varchar(100) NOT NULL,
  `Title` varchar(200) NOT NULL,
  `Category` enum('Billing','Service','Meter','Technical','Other') NOT NULL,
  `Status` enum('Open','In Progress','Resolved','Closed') DEFAULT 'Open',
  `Priority` enum('Low','Medium','High','Urgent') DEFAULT 'Medium',
  `CreatedDate` timestamp NOT NULL DEFAULT current_timestamp(),
  `AssignedTo` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `complaint`
--

INSERT INTO `complaint` (`ComplaintID`, `Subscriber`, `Title`, `Category`, `Status`, `Priority`, `CreatedDate`, `AssignedTo`) VALUES
('C-001', 'John Doe', 'Incorrect meter reading', 'Meter', 'Resolved', 'Medium', '2025-10-28 09:58:46', NULL),
('C-002', 'Patrick', 'Low water pressure', 'Service', 'Resolved', 'Medium', '2025-10-28 09:58:46', NULL),
('C-003', 'John Doe', 'Incorrect meter reading', 'Meter', 'Resolved', 'Medium', '2025-10-28 10:00:06', NULL),
('C-004', 'Patrick', 'Low water pressure', 'Service', 'In Progress', 'Medium', '2025-10-28 10:00:06', NULL),
('C-005', 'Allan', 'No electricity', 'Service', 'Open', 'Medium', '2025-10-28 10:00:06', NULL);

--
-- Triggers `complaint`
--
DELIMITER $$
CREATE TRIGGER `trg_complaint_before_insert` BEFORE INSERT ON `complaint` FOR EACH ROW BEGIN
    IF NEW.ComplaintID IS NULL OR NEW.ComplaintID = '' THEN
        SET NEW.ComplaintID = CONCAT(
            'C-',
            LPAD(
                IFNULL(
                    (SELECT MAX(CAST(SUBSTRING(ComplaintID, 3) AS UNSIGNED)) FROM complaint),
                    0
                ) + 1,
                3,
                '0'
            )
        );
    END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `meter`
--

CREATE TABLE `meter` (
  `id` int(11) NOT NULL,
  `Subscriber` varchar(100) NOT NULL,
  `Service` varchar(100) NOT NULL,
  `Unit` varchar(10) DEFAULT NULL,
  `Reading` decimal(10,2) NOT NULL,
  `Consumption` int(11) DEFAULT NULL,
  `Date` timestamp NOT NULL DEFAULT current_timestamp(),
  `Type` enum('Current','Previous') DEFAULT 'Current',
  `Status` enum('Pending','Verified','Overdue') DEFAULT 'Pending'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `meter`
--

INSERT INTO `meter` (`id`, `Subscriber`, `Service`, `Unit`, `Reading`, `Consumption`, `Date`, `Type`, `Status`) VALUES
(1, '1', '2', 'KWh', 120.50, 121, '2025-10-27 22:00:00', 'Current', 'Pending'),
(2, '1', '2', 'KWh', 120.50, 121, '2025-10-27 22:00:00', 'Current', 'Pending'),
(3, '1', '2', 'KWh', 123.45, 123, '2025-10-27 22:00:00', 'Current', 'Pending'),
(4, '1', '3', 'KWh', 150.75, 151, '2025-10-27 22:00:00', 'Current', 'Verified'),
(5, '2', '1', 'KWh', 200.50, 201, '2025-10-27 22:00:00', 'Current', 'Verified'),
(6, 'Sub-001', '2', 'KWh', 130.50, 131, '2025-10-27 22:00:00', 'Current', 'Verified'),
(7, 'Sub-001', '2', 'KWh', 130.50, 131, '2025-10-27 22:00:00', 'Current', 'Pending'),
(8, 'John Doe', 'Electricity', 'KWh', 0.00, 0, '2025-10-27 22:00:00', 'Current', 'Pending'),
(10, 'Bob Wilson', 'Gas', 'm3', 0.00, 0, '2025-10-27 22:00:00', 'Current', 'Pending'),
(11, 'Patrick', 'Water', 'm3', 0.00, 0, '2025-10-27 22:00:00', 'Current', 'Pending');

--
-- Triggers `meter`
--
DELIMITER $$
CREATE TRIGGER `trg_meter_before_insert` BEFORE INSERT ON `meter` FOR EACH ROW BEGIN
    IF NEW.MeterCode IS NULL OR NEW.MeterCode = '' THEN
        SET NEW.MeterCode = CONCAT('M-', LPAD((SELECT IFNULL(MAX(id),0)+1 FROM meter), 3, '0'));
    END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `payment`
--

CREATE TABLE `payment` (
  `PaymentID` int(11) NOT NULL,
  `BillID` varchar(10) DEFAULT NULL,
  `Amount` decimal(10,2) NOT NULL,
  `Date` timestamp NOT NULL DEFAULT current_timestamp(),
  `Method` enum('Credit Card','Bank Transfer','Cash','Mobile Money') NOT NULL,
  `Reference` varchar(100) NOT NULL,
  `Status` enum('Completed','Failed','Pending') DEFAULT 'Completed',
  `Subscriber` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `payment`
--

INSERT INTO `payment` (`PaymentID`, `BillID`, `Amount`, `Date`, `Method`, `Reference`, `Status`, `Subscriber`) VALUES
(4, 'B-003', 1000.00, '2025-10-28 08:17:24', 'Cash', 'Pay-001', 'Completed', '1'),
(5, 'B-003', 1000.00, '2025-10-28 08:31:55', 'Cash', 'Pay-002', 'Completed', '1');

--
-- Triggers `payment`
--
DELIMITER $$
CREATE TRIGGER `trg_payment_before_insert` BEFORE INSERT ON `payment` FOR EACH ROW BEGIN
  
  IF NEW.BillID IS NOT NULL THEN
    SET NEW.Subscriber = (SELECT Subscriber FROM bill WHERE BillID = NEW.BillID);
  END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `service`
--

CREATE TABLE `service` (
  `ServiceID` int(11) NOT NULL,
  `Name` varchar(100) NOT NULL,
  `Description` text DEFAULT NULL,
  `Category` enum('Water','Electricity','Gas','Internet','Maintenance') NOT NULL,
  `Price` decimal(10,2) NOT NULL,
  `Status` enum('Active','Inactive') DEFAULT 'Active',
  `CreatedAt` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `service`
--

INSERT INTO `service` (`ServiceID`, `Name`, `Description`, `Category`, `Price`, `Status`, `CreatedAt`) VALUES
(1, 'Residential Water', 'Monthly water supply', 'Water', 45.00, 'Inactive', '2025-10-25 08:18:24'),
(2, 'Electricity', 'Monthly electricity service', 'Electricity', 120.00, 'Active', '2025-10-25 08:18:24'),
(3, 'Natural Gas', 'Residential gas supply', 'Gas', 85.50, 'Active', '2025-10-25 08:18:24'),
(4, 'Watericys', 'Water supply', 'Water', 356367.00, 'Active', '2025-10-28 22:05:32'),
(5, 'Oiling', 'Oiling services free delivery', 'Water', 500000.00, 'Active', '2025-10-29 05:21:13');

-- --------------------------------------------------------

--
-- Table structure for table `subscriber`
--

CREATE TABLE `subscriber` (
  `SubscriberID` int(11) NOT NULL,
  `Username` varchar(50) NOT NULL,
  `PasswordHash` varchar(255) NOT NULL,
  `Email` varchar(100) NOT NULL,
  `FullName` varchar(100) NOT NULL,
  `Role` enum('Admin','Subscriber') DEFAULT 'Subscriber',
  `CreatedAt` timestamp NOT NULL DEFAULT current_timestamp(),
  `LastLogin` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `subscriber`
--

INSERT INTO `subscriber` (`SubscriberID`, `Username`, `PasswordHash`, `Email`, `FullName`, `Role`, `CreatedAt`, `LastLogin`) VALUES
(1, 'admin', 'admin123', 'admin@utilities.com', 'System Administrator', 'Admin', '2025-10-25 08:18:15', '2025-12-17 05:09:00'),
(2, 'john_doe', 'doe123', 'john.doe@email.com', 'John Doe', 'Subscriber', '2025-10-25 08:18:15', '2025-10-28 05:51:31'),
(3, 'jane_smith', '245jane', 'jane.smith@email.com', 'Jane Smith', 'Subscriber', '2025-10-25 08:18:15', '2025-10-13 07:04:02'),
(4, 'Ivan', '12345', 'ivan@gmail.com', 'GATETE Ivan', 'Subscriber', '2025-10-27 16:38:00', '2025-12-16 04:37:53'),
(5, 'Patrick', '12345', 'patrick@gmail.com', 'NIYORUREMA Patrick', 'Admin', '2025-10-28 17:11:50', '2025-10-29 07:07:30'),
(6, 'allan', 'allan12', 'allan@gmail.com', 'Allan', 'Subscriber', '2025-10-28 17:11:50', '2025-12-02 10:31:19'),
(8, 'uwase', '0987', 'monike@gmail.com', 'UWASE Monique', 'Subscriber', '2025-10-28 22:04:57', '2025-10-29 06:57:08'),
(10, 'vincet', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'vencet@gmail.com', 'KAMURASE Vencit', 'Subscriber', '2025-10-30 09:47:53', NULL);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `bill`
--
ALTER TABLE `bill`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `Reference` (`Reference`),
  ADD UNIQUE KEY `BillID` (`BillID`),
  ADD KEY `SubscriberID` (`Subscriber`),
  ADD KEY `bill_ibfk_2` (`Services`);

--
-- Indexes for table `complaint`
--
ALTER TABLE `complaint`
  ADD PRIMARY KEY (`ComplaintID`);

--
-- Indexes for table `meter`
--
ALTER TABLE `meter`
  ADD PRIMARY KEY (`id`),
  ADD KEY `SubscriberID` (`Subscriber`),
  ADD KEY `ServiceID` (`Service`);

--
-- Indexes for table `payment`
--
ALTER TABLE `payment`
  ADD PRIMARY KEY (`PaymentID`),
  ADD UNIQUE KEY `Reference` (`Reference`),
  ADD KEY `fk_payment_bill` (`BillID`),
  ADD KEY `fk_payment_subscriber` (`Subscriber`);

--
-- Indexes for table `service`
--
ALTER TABLE `service`
  ADD PRIMARY KEY (`ServiceID`);

--
-- Indexes for table `subscriber`
--
ALTER TABLE `subscriber`
  ADD PRIMARY KEY (`SubscriberID`),
  ADD UNIQUE KEY `Username` (`Username`),
  ADD UNIQUE KEY `Email` (`Email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `bill`
--
ALTER TABLE `bill`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- AUTO_INCREMENT for table `meter`
--
ALTER TABLE `meter`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `payment`
--
ALTER TABLE `payment`
  MODIFY `PaymentID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `service`
--
ALTER TABLE `service`
  MODIFY `ServiceID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `subscriber`
--
ALTER TABLE `subscriber`
  MODIFY `SubscriberID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `payment`
--
ALTER TABLE `payment`
  ADD CONSTRAINT `fk_payment_bill` FOREIGN KEY (`BillID`) REFERENCES `bill` (`BillID`),
  ADD CONSTRAINT `fk_payment_subscriber` FOREIGN KEY (`Subscriber`) REFERENCES `bill` (`Subscriber`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
