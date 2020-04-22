package cli;

import menu.Destination;
import menu.Meal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import simulation.Settings;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

//Works on generating the orders for the simulation
public class OrderGenerator {
    File nameFile; //File that contains a list of names
    ArrayList<String> names; //Stores the list of names
    int MINUTES_IN_SIM;
    SimController simController;
    File ordersFile; //xml file that saves the orders

    public OrderGenerator(SimController simController) {
        names = new ArrayList<>(); //List of names
        //simController = SimController.getInstance();
        MINUTES_IN_SIM = simController.getNUMBER_OF_SIMULATIONS();
        try {
            //Names.txt was generated by Dominic Tarr
            nameFile = new File("Names.txt"); //open the names file

            //Seed the arraylist with names in the file
            Scanner s = new Scanner(nameFile);
            while (s.hasNext()) {
                names.add(s.next());
            }
            s.close();

        } catch (Exception e) {
            System.out.println((e.getMessage()));
        }
    }

    public void generateOrders() {
        Random random = new Random(); //new random generator

        int minutesInSim = 240; //The number of minutes in the simulation
        int curMin = 0; //The current minute the simulation is in
        int randName; //random integer for what name to choose the ArrayList
        ArrayList<Integer> ordersPerHour = Settings.getOrderDistribution(); //Get the ordersPerHour
        ArrayList<Destination> map = Settings.getMap(); //Map of the given campus
        Meal m; //Current meal being ordered
        Destination d; //The destination to be delivered to


        //open the orders file
        try {
            ordersFile = new File("Orders.xml");
            FileWriter fileWriter = new FileWriter(ordersFile, false); //clear out the orders file
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println("<listOfOrders>");
            printWriter.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        //Get the adjusted probability of an order coming in
        double chanceOfOrderPerM = adjustProbability(ordersPerHour.get(0));

        //For each minute in the simulation
        while (curMin < MINUTES_IN_SIM) {

            if (random.nextDouble() < chanceOfOrderPerM) { //If the probability is low enough, generate an order
                //Get a random name
                randName = random.nextInt(names.size());

                //Get a random meal based on the distribution
                m = randomMeal(random.nextDouble());

                //Get a random destination
                d = map.get(random.nextInt(map.size()));

                if (m != null) {
                    //Create a new order
                    PlacedOrder ord = new PlacedOrder(curMin, d, names.get(randName), m);

                    //Add the order to the xml file
                    ord.addToXML(ordersFile);
                } else {
                    System.out.println("randomMeal is broken\n");
                }//m!=null

            } else { //If an order is not generated
                curMin++;

                //Adjust the probability if we shift to a new hour
                if (curMin % 60 == 0 && curMin < MINUTES_IN_SIM) {
                    chanceOfOrderPerM = adjustProbability(ordersPerHour.get(curMin / 60));
                }
            }


        }

        //Add the xml closing for the orders file
        try {
            ordersFile = new File("Orders.xml"); //open the orders file
            FileWriter fileWriter = new FileWriter(ordersFile, true);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println("</listOfOrders>");
            printWriter.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Adjust the probability so that generateOrders produces the correct number of orders
     * even though multiple orders can come in a single minute
     *
     * @param ordersPerHour The number of orders per this hour
     * @return The adjusted probability
     */
    private double adjustProbability(int ordersPerHour) {
        //The probability that we would be shooting for if we were not allowing multiple orders to
        //come in the same minute
        double idealProbability = ordersPerHour / 60.0;

        double newProbability = idealProbability; //The new probability that we will use

        //The sum of the geometric series that is used to find what the input probability will correspond
        //to given that we can have multiple orders per minutes
        double extrapolatedProbability = Double.MAX_VALUE;

        //Keep looping until we lower the newProbability enough so that it will result in the correct
        //number of orders per hours
        while (extrapolatedProbability > idealProbability) {
            newProbability -= .01;
            extrapolatedProbability = newProbability / (1 - newProbability); //Sum of a geometric series
        }

        return newProbability;
    }

    /**
     * Get all of the order in the xml file
     *
     * @return ArrayList of orders that were placed
     */
    public ArrayList<PlacedOrder> getXMLOrders() {
        ArrayList<PlacedOrder> placedOrders = new ArrayList<>(); //all the placed orders

        //List of potential meals so that to compare the name in the xml file with
        List<Meal> meals = Settings.getMeals();

        try {
            //XML file reading initialization
            File orderFile = new File("Orders.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newDefaultInstance();
            DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(orderFile);
            document.getDocumentElement().normalize();

            //Create a node list of all the elements
            NodeList nodeList = document.getElementsByTagName("order");

            //Variables that go in a placed order
            String cname, dname, mealString, mname;
            int ordTime;
            Destination dest;
            Meal chosenMeal = new Meal("Temp", 0);
            PlacedOrder oneOrder;

            //For each element in the file
            for (int temp = 0; temp < nodeList.getLength(); temp++) {

                Node node = nodeList.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) node;

                    //Get the customer's name
                    cname = e.getElementsByTagName("cname").item(0).getTextContent();

                    //Get the time of the order
                    ordTime = Integer.parseInt(e.getElementsByTagName("ordTime").item(0).getTextContent());

                    //Get the destination
                    dname = e.getElementsByTagName("dest").item(0).getTextContent();
                    Scanner destScanner = new Scanner(dname);
                    destScanner.useDelimiter("\t");
                    dest = new Destination(destScanner.next(), destScanner.nextInt(), destScanner.nextInt());
                    destScanner.close();

                    //Get the name of the meal in the file and check it against the potential meals and find
                    //the right meal
                    mealString = e.getElementsByTagName("meal").item(0).getTextContent();
                    Scanner mealScanner = new Scanner(mealString);
                    mealScanner.useDelimiter(":");
                    mname = mealScanner.next();
                    mealScanner.close();
                    for (int m = 0; m < meals.size(); m++) {
                        if (mname.equals(meals.get(m).getName())) {
                            chosenMeal = meals.get(m);
                            break;
                        }
                    }


                    //Create the order and it to the arrayList of all the orders
                    oneOrder = new PlacedOrder(ordTime, dest, cname, chosenMeal);
                    placedOrders.add(oneOrder);
                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
        return placedOrders;
    }

    /**
     * Returns a random meal based on the distribution
     *
     * @param rand A random double between 0 and 1
     * @return The meal that was randomly selected
     */
    private Meal randomMeal(double rand) {
        double counter = 0; //Keeps track of the distribution through the loop

        //Store the list of meals
        List<Meal> lm = Settings.getMeals();

        //Iterate to where the random number points to in the distribution
        for (int i = 0; i < lm.size(); i++) {
            counter += lm.get(i).getDistribution();
            if (rand < counter) {
                return lm.get(i); //return the meal selected
            }
        }

        //If it returns null the distribution invalid
        return null;
    }

}
