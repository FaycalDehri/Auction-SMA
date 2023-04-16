import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

public class VendeurAgent extends Agent {
    private int prixDepart;
    private int prixReserve;
    private int meilleurOffre;
    private String gagnant;

    protected void setup() {
        // Récupérer le prix de départ et le prix de réserve depuis l'argument du programme
        Object[] args = getArguments();
        if (args != null && args.length > 1) {
            prixDepart = Integer.parseInt((String) args[0]);
            prixReserve = Integer.parseInt((String) args[1]);
           // System.out.println(prixDepart+prixReserve);
        } else {
            // Si les arguments sont manquants, définir des valeurs par défaut
            prixDepart = 10;
            prixReserve = 50;
        }

        long startTime = System.currentTimeMillis();
        long elapsedTime;
        meilleurOffre = 0;


        // Ajouter un comportement cyclique pour gérer les offres
        // Ajouter un comportement cyclique pour gérer les offres
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {

                // Recevoir les messages des acheteurs
                ACLMessage msg = receive();

                if (msg != null) {
                    // Vérifier si l'offre est valide et supérieure au meilleur offre actuel
                    int offre = Integer.parseInt(msg.getContent());
                   // System.out.println("Agent "+msg.getSender().getLocalName()+" a proposé un refus de "+offre+"$");


                    if (offre > meilleurOffre) {
                        System.out.println(msg.getSender().getLocalName()+" a proposé "+offre+"$");
                        meilleurOffre = offre;
                        gagnant = msg.getSender().getLocalName();
                        // Envoyer le nouveau meilleur offre à tous les acheteurs
                        DFAgentDescription template = new DFAgentDescription();
                        ServiceDescription sd = new ServiceDescription();
                        sd.setType("Acheteur");
                        template.addServices(sd);
                        try {
                            DFAgentDescription[] result = DFService.search(myAgent, template);
                            for (DFAgentDescription dfAgentDescription : result) {
                                if(!dfAgentDescription.getName().getLocalName().equals(gagnant)){
                                    ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                                    reply.addReceiver(dfAgentDescription.getName());
                                    reply.setContent(String.valueOf(meilleurOffre));
                                    send(reply);
                                }

                            }
                        } catch (FIPAException fe) {
                            fe.printStackTrace();
                        }
                    }
                } else {
                    block();
                }
            }
        });



    }

    protected void takeDown() {
        // Vendre l'objet si le dernier offre est supérieur au prix de réserve
        if (meilleurOffre >= prixReserve) {
            System.out.println("Vendu à " + getAID().getName() + " pour " + meilleurOffre);
        } else {
            System.out.println("Pas de vente. Le dernier offre était " + meilleurOffre);
        }
        doDelete();
    }
}
