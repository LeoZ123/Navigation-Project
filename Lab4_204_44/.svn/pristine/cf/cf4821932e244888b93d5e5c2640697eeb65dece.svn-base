package ca.uwaterloo.lab3_204_44;

import android.graphics.PointF;
import android.widget.TextView;
import mapper.*;
import java.util.*;

public class PositionListener implements IMapperListener {
	int count = 0;
	public int calibrated_angle = -180;
	public Mapper map;
	static final int UNIT_STEP = 2;
	boolean pointsSet = false;
	boolean pathSimplified = false;
	int pathSimplifycount = 0;
	TextView directionTextView;
	
	int debug_count = 0;
	float test = 0;
	float test2 = 15;
	
	public PositionListener(Mapper m, TextView directionTextView) {
		map = m;
		this.directionTextView = directionTextView;
	}
	
	
	
//--------------------------------------------------------------------------------------------------------
	/*CalculateApparentNextPath
	 * 		Takes in source and the current angle
	 * 		Using the calibrated angle (where calibrated angle means facing perpendicular to blackboard)
	 * 		calculateApparentNextPath will use basic components (sin/cos) create a boundary point.
	 * 
	 * 		Create a line segment from the userPoint and BoundaryPoint, and displays it on the screen.
	 */
	
	
	public void calculateApparentNextPath(Mapper source, float angle) {
		
		
		double angle_adjusted_RAD = Math.toRadians(angle + 180 - calibrated_angle);
		
		
		source.currentPath.clear();
		source.currentPath.add(new LineSegment(
				new PointF(source.getUserPoint().x, source.getUserPoint().y),
				new PointF((float) (source.getUserPoint().x + 33f*Math.sin(angle_adjusted_RAD)), 
						(float) (source.getUserPoint().y - 33f*Math.cos(angle_adjusted_RAD)))
				
				)
				);
		source.invalidate();	
	}
//--------------------------------------------------------------------------------------------------------


//--------------------------------------------------------------------------------------------------------
	/*onStep
	 * 
	 * Takes in the map, and current angle
	 * 
	 * Using the calibrated angle (angle perpendicular to the blackboard), generate the next
	 * point that will be taken by the user in the specific direction.
	 * 
	 * using "CalculateIntersections", if that path has a wall intercepting, then go as close
	 * as you reasonably can to the wall, using the function "SpaceNextPoint"
	 * 
	 * 
	 * 
	 */
	
	public void onStep(Mapper source, float angle) {
		
		if (pointsSet == true) {
			PointF currentPoint = source.getUserPoint();
			
			PointF nextPoint = new PointF();
			
			int angle_post_calibration = (int) (angle + 180 - calibrated_angle);
			System.out.println(calibrated_angle + " " + angle_post_calibration);
			nextPoint.x = 	(float) (currentPoint.x + UNIT_STEP*Math.sin(Math.toRadians(angle_post_calibration)));
			nextPoint.y =	(float) (currentPoint.y - UNIT_STEP*Math.cos(Math.toRadians(angle_post_calibration)));
			
			List<InterceptPoint> Intersections = source.calculateIntersections(currentPoint, nextPoint);
			if (Intersections.size() < 1) {
				locationChanged(source, nextPoint);
			} else {
				PointF closestPoint = new PointF();
				spaceNextPoint(new LineSegment(currentPoint, Intersections.get(0).getPoint()), currentPoint, closestPoint);
					locationChanged(source, closestPoint);

					

			}
		}
	}

//--------------------------------------------------------------------------------------------------------

	/*
	 * locationChanged
	 * 
	 * Takes as arguments the map and newest point that user has been placed
	 * 
	 * This code will set the userPoint equal to in parameter "loc"
	 * Furthermore, it supports path finding:
	 * 1) First it will generate orthogonal paths from the userPoint
	 * 2) Next, given that a desination exists,
	 * 	  the function will call "generatePath" which will recursively determine a path towards
	 *    the destination point
	 * 3) Finally, we call the function "simplifyPath" to eradicate any unnecessary turning
	 *
	 * 
	 */
	
	
	
	@Override
	public void locationChanged(Mapper source, PointF loc) {

		source.setUserPoint(loc);								//Set the UserPoint
		source.setUserPath(null);								//Reset our User Path
		
		source.userPointRadials.clear();						//Clear the radial paths of userPoint
		generateRadials(loc,source,false);						//Regenerate the radial paths of userPoint
		
		//If both our points are set
		if (pointsSet == true) {
			
			
			//Generate a path object, pastPoints, adding the current point the path and pastPoints
			//Before calling generatePath.
			
			List<PointF> path = new ArrayList<PointF>();
			List<PointF> pastPoints = new ArrayList<PointF>();
			path.add(source.getUserPoint());
			pastPoints.add(new PointF(Math.round(source.getUserPoint().x),Math.round(source.getUserPoint().y)));
			generatePath(source, path, pastPoints, source.getUserPoint());
			
			
			//Call for simplification on the path
			pathSimplified = false;
			pathSimplifycount = 0;
			while (pathSimplified == false || pathSimplifycount == 15) {
			source.userPath = simplifyPath(source);
			pathSimplifycount++;
			}
		}

		//Update the textView if we are within proximity of our Unit Step 
		if (FloatHelper.distance(source.getUserPoint(), source.getEndPoint()) <= UNIT_STEP) {
			source.setUserPoint(new PointF(source.getEndPoint().x, source.getEndPoint().y));
			directionTextView.setText("You are at your Destination!");
		} else {
			directionTextView.setText("Align your Compass Line with the Path then Step Forward!");
		}
		
		count = 0;
	}
	
//-----------------------------------------------------------------------------------------------------

/*
 * DestionationChanged
 * 
 * Takes as parameters the map and destination point
 */
	
	@Override
	public void DestinationChanged(Mapper source, PointF dest) {
		count = 0;
		
		source.setUserPath(null);								//Reset our User Path

		
		//Generate radial paths from our destination
		source.destinationRadials.clear();
		generateRadials(source.getEndPoint(),source,true);

		
		
		
		if (source.getStartPoint() != null) {
			pointsSet = true;					//Note that both points are set on the map!
			
			//Generate a path object, pastPoints, adding the current point the path and pastPoints
			//Before calling generatePath.
			
			List<PointF> path = new ArrayList<PointF>();
			List<PointF> pastPoints = new ArrayList<PointF>();
			path.add(source.getUserPoint());
			pastPoints.add(new PointF(Math.round(source.getUserPoint().x),Math.round(source.getUserPoint().y)));
			generatePath(source, path, pastPoints, source.getUserPoint());
			
			
			//Call for simplification on the path
			pathSimplified = false;
			pathSimplifycount = 0;
			while (pathSimplified == false || pathSimplifycount == 15) {	//POTENTIAL BUG HERE!!!
				source.userPath = simplifyPath(source);
				pathSimplifycount++;
			}
			}
		
	}
	
//-----------------------------------------------------------------------------------------------------

/*
 * generatePath
 * 
 * Takes in as parameters the map, the current path it is generating, previous points it has past and it's
 * current point that it resides at.
 * 
 * This is the crux of pathing algorithm broken into essentially three major steps.
 * 
 * (1) path = the current worked on path that will eventually get the destination
 * (2) pastPoints = central points that we have passed. The pathing algorithm is not allowed go to points
 * 					that are within a specific radius of those past points
 * (3) currentPoint = the latest point in the path at the moment of the recursive call
 * 
 * 
 * The algorithm takes three cases essentially:
 * 
 * %%%%  CASE 1: (DIRECT PATH) %%%%
 * 
 * 			If there is a direct path from the currentPoint and destination:
 * 				- Add the destination point to the path
 * 				- DO NOT, make any recursive calls on generatePath. Traverse up the call stack 
 *            	and exit generatePath
 * 
 * %%%% CASE 2: (INTERSECTING RADIALS) %%%%
 * 			Remember that both the userPoint and the destination have orthogonal radials
 * 		
 * 			If the radials of the userPoint intersect the radials of the destination, then we KNOW 
 * 			that the path can go up to that intersection point of the two line segments. 
 * 		
 * 			After that, we are GUARANTEED to have a direct path to the destination.
 * 
 * 
 * 			-Double For Loop between the user Radials and destination Radials
 * 				- Using the LineSegment class, check for an interecpt. If there exists an intercept,
 * 		    	  then add that point to the path, pastpoints
 * 				- RECURSIVELY call on generatePath, setting "currentPoint" as the point we had just found
 * 		
 * 
 * %%%% CASE 3: (BEST POSSIBLE PATH) %%%%
 * 
 * 		At this point, there are no direct paths. Take one of the radial paths. Here is how to choose:
 * 
 * 		For each of 4 radial paths:
 * 			- Check if the end point to that radial path is forbidden (i.e. in radius of one of the points
 * 			  in "pastPoints"
 * 			- Calculate the Euclidean Distance from the end point to the destination, and choose
 * 			  the best closest path. 
 * 			- Call generatePath recursively on this new point, adding it to both path and pastPoints
 * 			
 * 
 * 
 * This code will continue to recurse until it either finds the destination or goes through 50 iterations
 * (This is to ensure that if not path exists, it will not get stuck in infinite recursion)
 */

	
	public void generatePath(Mapper source, List<PointF> path, List<PointF> pastPoints, PointF currentPoint) {
	

		
		source.userPointRadials.clear();
		generateRadials(currentPoint, source, false);
		Boolean pointFound = false;
		
		
		
		//CASE 1: (DIRECT PATH)
		if (source.calculateIntersections(currentPoint, source.getEndPoint()).size() < 1) {	//Check Intersections
			
			//Add to path and pastPoints, pointFound = true to ignore any other logic
			
			path.add(source.getEndPoint());						
			pastPoints.add(source.getEndPoint());
			pointFound = true;
		
		} else {
			
			//CASE 2: (INTERSECTING RADIALS)
		
			//Loop through userRadials and destinationRadials
			Outerloop:
			for (LineSegment userL: source.userPointRadials) {
				for (LineSegment destL : source.destinationRadials) {
					
					PointF interceptPoint = userL.findIntercept(destL);	//Try to find an intercept
					
					if (interceptPoint != null) {					//If there is an intersection
							path.add(interceptPoint);				//Add to path, repeat process

							
							//Found an intercept, so set pointFound = true, call generatePath
							
							pastPoints.add(new PointF(Math.round(interceptPoint.x),Math.round(interceptPoint.y)));
							pointFound = true;
							source.userPointRadials.clear();
							generateRadials(interceptPoint, source, false);
							
							
							//Rercursively call on generatePath with our new point!
							
							if (count <= 50) {
								count++;
							generatePath(source, path, pastPoints, interceptPoint);
							
							}
							break Outerloop;
							
						}
				}
			}
			
		
		
		 //CASE 3: (BEST POSSIBLE PATH)
			
			if (!pointFound) {
				//If not, then choose the lowest cost path that is not on the pastPoints list
				
				//Set the cost equal to max value, randomly initialize lowestCostPath

				LineSegment lowestCostPath = new LineSegment(currentPoint, currentPoint);
				float cost = Float.MAX_VALUE;
				
				
				//Iterate through every possible path and determine the lowest cost Path;
				for (LineSegment userL: source.userPointRadials) {
					
					//Check that the point is not within a radius of our past Points
					//This is done by simplying rounding our numbers to the nearest integer
					
					if (!(pastPoints.contains(new PointF(Math.round(userL.end.x),Math.round(userL.end.y))))) {
						
						if (FloatHelper.distance(source.getUserPoint(), userL.end) > UNIT_STEP) {
							float dist = FloatHelper.distance(userL.end, source.getEndPoint()); 
							
							//Keep running count of the lowest distance == "lowest cost path"
							if (dist < cost) {
								cost = dist;
								lowestCostPath = userL;
							}
						}
					} 
					
				}

				
				/*
				 * Calls spaceNextPoint:
				 * This effectively ensures that our nextPoint does not go straight INTO a wall
				 * But instead keeps a minimal distance so as to not get stuck
				 */
				PointF newpoint = new PointF();
				spaceNextPoint(lowestCostPath, currentPoint, newpoint);
				
				
				
				path.add(newpoint);
				pastPoints.add(new PointF(Math.round(lowestCostPath.end.x),Math.round(lowestCostPath.end.y)));
				pastPoints.add(new PointF(Math.round(newpoint.x),Math.round(newpoint.y)));
				
				source.userPointRadials.clear();
				generateRadials(newpoint, source, false);
				
				//Recursively call on generatePath on the newpoint we have found!
				if (count <= 50) {
					count++;
					generatePath(source, path, pastPoints, newpoint);
				}
				}

			}

		//Once we have finally gotten out of the recursive hole, set the UserPath!
		source.setUserPath(path);
		count = 0;
	}
			
	
	/*
	 * SpaceNextPoint
	 * 
	 * Takes as parameters lowestCostPath, currentPoint and newPoint
	 * 
	 * This code snippet essentially makes sure that when we choose the shortest path we can
	 * towards a wall, that our endpoint is not the wall exactly, but a short distance away
	 * 
	 * This makes sure we do not get stuck in our wall!
	 */
	
	public void spaceNextPoint(LineSegment lowestCostPath, PointF currentPoint, PointF newpoint) {
		
		if (lowestCostPath.end.x < currentPoint.x && lowestCostPath.end.y < currentPoint.y) {
			newpoint.x = lowestCostPath.end.x + 0.5f;
			newpoint.y = lowestCostPath.end.y + 0.5f;
		} else if (lowestCostPath.end.x < currentPoint.x && lowestCostPath.end.y > currentPoint.y) {
			newpoint.x = lowestCostPath.end.x + 0.5f;
			newpoint.y = lowestCostPath.end.y - 0.5f;
		} else if (lowestCostPath.end.x > currentPoint.x && lowestCostPath.end.y < currentPoint.y) {
			newpoint.x = lowestCostPath.end.x - 0.5f;
			newpoint.y = lowestCostPath.end.y + 0.5f;
		} else if (lowestCostPath.end.x > currentPoint.x && lowestCostPath.end.y > currentPoint.y) {
			newpoint.x = lowestCostPath.end.x - 0.5f;
			newpoint.y = lowestCostPath.end.y - 0.5f;
		} else if (lowestCostPath.end.x > currentPoint.x) {
			newpoint.x = lowestCostPath.end.x - 0.5f;
			newpoint.y = lowestCostPath.end.y;
		}  else if (lowestCostPath.end.x < currentPoint.x) {
			newpoint.x = lowestCostPath.end.x + 0.5f;
			newpoint.y = lowestCostPath.end.y;
		}  else if (lowestCostPath.end.y > currentPoint.y) {
			newpoint.y = lowestCostPath.end.y - 0.5f;
			newpoint.x = lowestCostPath.end.x;
		}  else if (lowestCostPath.end.y < currentPoint.y) {
			newpoint.y = lowestCostPath.end.y + 0.5f;
			newpoint.x = lowestCostPath.end.x;
		}
		
		 
		 
	}
	
//--------------------------------------------------------------------------------------------------------
	

	/*simplifyPath
	 * 
	 * Takes as arguments the map
	 * 
	 * The purpose of this code is to simplify the path that may have many awkward turns 
	 * 
	 * This algorithm is done by iterating from the first possible point on the path and iterating backwards from the last lineSegment
	 * 
	 * For each point, count down from the line segments and see if that point's orthogonal radials intersect that line segment
	 * If so, then create a simplified path that goes from that current point to the intersection and as previously stored in the
	 * original path to the destination
	 * 
	 * This is called repetitively in a for loop until we hit the return statement at the botton, where we set "pathSimplified = true"
	 * to tell us that the path is as simplified as we can get and can exit the loop
	 */
	
	
	public List<PointF> simplifyPath(Mapper source) {
		
		//Start by generating some useful variables
		int pointListSize = source.userPath.size();
		
		List<LineSegment> pathSegments = new ArrayList<LineSegment>();
		List<PointF> simplifiedPath = new ArrayList<PointF>();
		
		
		//Generate Every Line Segment that connects pairs of adjacent points in userPath
		for (int i = 0; i < pointListSize - 1; i++) {
			pathSegments.add(new LineSegment(
					source.userPath.get(i),
					source.userPath.get(i+1)));
		}
		
		int pathSegmentSize = pathSegments.size();
		
		
		for (int j = 0; j < pointListSize - 1; j++) {
			PointF currentPoint = source.userPath.get(j);
			source.userPointRadials = null;
			generateRadials(currentPoint, source, false);
			
			//Check if j+2 > the amount of segments we have
			
			if (!(j+2 > pathSegmentSize-1)) {
				
			
				
				//Iterate from the farthest upto the j + 2nd path. This ensures that none of the paths
				//That we will explore are in direct intersection with radials of our jth point
				
				
				for (int k = pathSegmentSize - 1; k >= j + 2 ; k--) {
					
					LineSegment currentPath = pathSegments.get(k);
				
					
					//For every radial for our jth point
					for (LineSegment radial: source.userPointRadials) {
						
						//Check if there is an interception
						PointF interceptPoint = radial.findIntercept(currentPath);
						
						//If there is an interception: Add to our simplified path
						if (interceptPoint != null) {
							
							simplifiedPath.add(currentPoint);
							simplifiedPath.add(interceptPoint);
							
							for (int l = k+1; l < pointListSize; l++) {
								simplifiedPath.add(source.userPath.get(l));
							}
							
							return simplifiedPath; //Returns the simplified path!
							
						}
						
						
					}
					
					
					
				}
			} //END OF THE IF STATEMENT
			
		
			simplifiedPath.add(currentPoint); 
		}
		
		
		
		
		
		
		
		pathSimplified = true;
		return source.userPath; //Returns the userPath at this point if we haven't returned it earlier
		//Meaning that we are returning the same path we found.
	}
		
	
	
	/*
	 * Generate Radials
	 * 
	 * Takes as arguments the point that we will generate radials from, the map, and a boolean that states whether this is for
	 * destination radials or currentPoint radials
	 * 
	 * This code simply creates 4 Line Segments that point to the maximum above, below, left and right of the current point
	 * 
	 * This is done by calling "CalculateIntersections" between that current point and the farthest possible point,
	 * and generating a line segment that goes from the current point to the first wall intersection it finds!
	 * 
	 */
	
	public void generateRadials(PointF centre, Mapper source, Boolean isDest) {
		
		List<InterceptPoint> Intercepts = new ArrayList<InterceptPoint>();
		List<LineSegment> Radials = new ArrayList<LineSegment>();
		
		//Left Generator
		Intercepts = source.calculateIntersections(centre, new PointF(0,centre.y));
		Radials.add(new LineSegment(centre, Intercepts.get(0).getPoint()));
		
		//Right Generator
		Intercepts = source.calculateIntersections(centre, new PointF(25,centre.y));
		Radials.add(new LineSegment(centre, Intercepts.get(0).getPoint()));

		//Upper Generator
		Intercepts = source.calculateIntersections(centre, new PointF(centre.x,0));
		Radials.add(new LineSegment(centre, Intercepts.get(0).getPoint()));
		
		//Lower Generator
		Intercepts = source.calculateIntersections(centre, new PointF(centre.x,22));
		Radials.add(new LineSegment(centre, Intercepts.get(0).getPoint()));
		
	
		//Stores the radials as "userPointRadials" or as "destinationRadials" based on which one we want to create!
		if (!(isDest)) {
			source.userPointRadials = Radials;
		} else {
			source.destinationRadials = Radials;
		}
	}

}
