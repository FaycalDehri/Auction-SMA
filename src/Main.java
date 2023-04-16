import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;




public class Main {

    public static ArrayList<AID> listeDesAcheteurs;
    public static List<String> readNamesFromFile(String fileName) throws IOException {
        List<String> names = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line;
        while ((line = reader.readLine()) != null) {
            names.add(line.trim());
        }
        reader.close();
        return names;
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Entrez le prix de départ de l'objet : ");
        int prixDepart = scanner.nextInt();
        System.out.print("Entrez le prix de reserve de l'objet : ");
        int prixReserve = scanner.nextInt();
        System.out.print("Entrez le nombre d'acheteurs : ");
        int nombreAcheteurs = scanner.nextInt();

        List<String> namesList = readNamesFromFile("C:\\Users\\Fayçal\\OneDrive\\Bureau\\SII\\S2\\TECH_AGENTS\\TP\\Auction\\src\\names.txt");
        Collections.shuffle(namesList);

        if (nombreAcheteurs > 0) {
            namesList.subList(0, nombreAcheteurs).clear();
        }

        try {
            // Créer un conteneur pour les agents
            Runtime rt = Runtime.instance();
            Profile p = new ProfileImpl();
            p.setParameter(Profile.MAIN_HOST, "localhost");
            p.setParameter(Profile.GUI, "true");
            ContainerController cc = rt.createMainContainer(p);

            // Créer l'agent vendeur
            String[] argsVendeur = {String.valueOf(prixDepart), String.valueOf(prixReserve)};
            AgentController vendeurCtrl = cc.createNewAgent("vendeur", VendeurAgent.class.getName(), argsVendeur);
            vendeurCtrl.start();

            // Créer les agents acheteurs
            for (int i = 1; i <= nombreAcheteurs; i++) {
                Random random = new Random();
                int argentDePoche = random.nextInt(prixReserve*2 - prixDepart + 1) + prixDepart;
                System.out.println(namesList.get(0) + " a " + argentDePoche + "$");
                String[] argsAcheteur = {String.valueOf(argentDePoche), String.valueOf(prixDepart)};



                AgentController acheteurCtrl = cc.createNewAgent(namesList.remove(0), AcheteurAgent.class.getName(), argsAcheteur);
                acheteurCtrl.start();
            }
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        scanner.close();
    }
}
