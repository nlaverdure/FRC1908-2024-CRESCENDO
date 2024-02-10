package frc.robot.subsystems;

import java.util.Optional;

import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonCamera;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.PhotonPoseEstimator.PoseStrategy;
import org.photonvision.targeting.PhotonTrackedTarget;
import org.photonvision.PhotonUtils;

import edu.wpi.first.apriltag.AprilTag;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.apriltag.AprilTagPoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

/*
 * TODO: functions based on analyisis of apriltag data (pos data)
 * TODO: function that outputs path data for navigation of current pos to new pos
 * 
 */


public class VisionSubsystem extends SubsystemBase {
    
    private static final double CAMERA_HEIGHT_METERS = 0.4;

    private static final double CAMERA_PITCH_RADIANS = 0;

    private static final double TARGET_HEIGHT_METERS = 0.4;

    private DriveSubsystem m_DriveSubsystem;
    private boolean noteVisible = false;
    private boolean noteLocked = false;
    private boolean aprilTagVisible = false;
    private double noteSkew = 0;
    private double notePitch = 0;
    private double noteArea = 0;
    private double noteYaw = 0;

    PhotonCamera driverCamera = new PhotonCamera("Driver Camera");
    Transform3d driverTransform = new Transform3d(new Translation3d(0.4, 0, 0.4), new Rotation3d(0,0,0));
    PhotonCamera noteCamera = new PhotonCamera("Note Detection");
    Transform3d noteTransform = new Transform3d(new Translation3d(-0.4,0,0.15), new Rotation3d(0,0,0));
    
    Optional<EstimatedRobotPose> m_estimatedPose = null;
    //EstimatedRobotPose robotPose = new EstimatedRobotPose(null, 0, null, null);
    Pose2d odometryPose = null;
    AprilTagFieldLayout aprilTagFieldLayout = AprilTagFields.k2024Crescendo.loadAprilTagLayoutField();
    PhotonPoseEstimator poseEstimator = new PhotonPoseEstimator(aprilTagFieldLayout, PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR, driverCamera, driverTransform );
    PhotonTrackedTarget currentBestTarget = null;
    PhotonTrackedTarget currentLockedTarget = null;
    

    public void periodic()
    {
        m_estimatedPose = getEstimatedGlobalPose(odometryPose);
        if(m_estimatedPose.isPresent()) {
            m_DriveSubsystem.addVisionMeasurement(m_estimatedPose.get());
        }
        odometryPose = m_DriveSubsystem.getPose();
        if(currentLockedTarget != null) {
            notePitch = currentLockedTarget.getPitch();
            noteYaw = currentLockedTarget.getYaw();
            noteArea = currentLockedTarget.getArea();
            noteSkew = currentLockedTarget.getSkew();
            notePitch = SmartDashboard.getNumber("Note Pitch", notePitch);
            noteYaw = SmartDashboard.getNumber("Note Yaw", noteYaw);
            noteArea = SmartDashboard.getNumber("Note Area", noteArea);
            noteSkew = SmartDashboard.getNumber("Note Skew", noteSkew);

        }

    }



    public VisionSubsystem(DriveSubsystem drive) {
        m_DriveSubsystem = drive;
        odometryPose = m_DriveSubsystem.getPose();

    }

    public Optional<EstimatedRobotPose> getEstimatedGlobalPose(Pose2d prevEstimatedRobotPose) {
        poseEstimator.setReferencePose(prevEstimatedRobotPose);
        return poseEstimator.update();

    }



    //need to be implemented
    public boolean trackNote() {
        /* this function should return a boolean based on whether the camera's latest results have a note target.
         * place that note target in currentBestTarget
        */
        var noteLatestResult = noteCamera.getLatestResult();
        if (noteLatestResult.hasTargets()) {
            currentBestTarget = noteLatestResult.getBestTarget();
            return noteVisible = true;
        }
        else {
                currentBestTarget = null;
                return noteVisible = false;
        }
        
    }

    public boolean lockNote() {
        /* this function should return a boolean based on whether the current locked target is occupied. 
         * Normally set current locked target to current best target. 
         * if current best target is null, return false. 
         * generate stats about target position, to be stored in a pose2D.
        */

        if (currentBestTarget != null) {
            currentLockedTarget = currentBestTarget;
                //TODO: generate data about target field position
            return noteLocked = true;
            
        }
        else {
            currentBestTarget = null;
            return noteLocked = false;
        }
       
    }

    public void targetRetrieved() {
        currentLockedTarget = null;

    }

    public boolean noteVisible(){
        var result1 = noteCamera.getLatestResult();
        if (result1.hasTargets()) {
                    // First calculate range
          double range =
                PhotonUtils.calculateDistanceToTargetMeters(
                        CAMERA_HEIGHT_METERS,
                        TARGET_HEIGHT_METERS,
                        CAMERA_PITCH_RADIANS,
                        Units.degreesToRadians(result1.getBestTarget().getPitch()));

         
        }
        return noteVisible = true;
    }
       


        

        
    

    public Command waitForRing() {
        return Commands.waitUntil(() -> noteVisible());
    }
    





}