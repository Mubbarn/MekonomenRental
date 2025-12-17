package mekonomen.service;

import mekonomen.model.Item;
import mekonomen.model.Member;
import mekonomen.model.Rental;
import mekonomen.repo.Inventory;
import mekonomen.repo.MemberRegistry;

import java.io.*;
import java.nio.file.*;
import java.util.List;

public class DataStore {

    private final Path baseDir;
    private final Path file;

    public DataStore(String folderName) {
        this.baseDir = Paths.get(folderName);
        this.file = baseDir.resolve("mekonomen.dat");
        ensureDir();
    }

    private void ensureDir() {
        try {
            if (!Files.exists(baseDir)) {
                Files.createDirectories(baseDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Kunde inte skapa data-mapp", e);
        }
    }


    public void saveAll(MemberRegistry members, Inventory inventory, RentalService rentals) {
        StoreBlob blob = new StoreBlob(
                members.getAll(),
                inventory.getAll(),
                rentals.getAllRentals()
        );

        try (ObjectOutputStream out =
                     new ObjectOutputStream(new FileOutputStream(file.toFile()))) {
            out.writeObject(blob);
        } catch (IOException e) {
            throw new RuntimeException("Kunde inte spara data", e);
        }
    }


    public void loadAll(MemberRegistry members, Inventory inventory, RentalService rentals) {
        if (!Files.exists(file)) return;

        try (ObjectInputStream in =
                     new ObjectInputStream(new FileInputStream(file.toFile()))) {

            StoreBlob blob = (StoreBlob) in.readObject();

            members.setAll(blob.members);
            inventory.setAll(blob.items);
            rentals.setAllRentals(blob.rentals);

            for (Rental r : blob.rentals) {
                if (r.isActive()) {
                    Item it = inventory.findById(r.getItemId());
                    if (it != null) it.setAvailable(false);
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Kunde inte läsa data", e);
        }
    }


    private static class StoreBlob implements Serializable {
        private static final long serialVersionUID = 1L;

        private final List<Member> members;
        private final List<Item> items;
        private final List<Rental> rentals;

        private StoreBlob(List<Member> members, List<Item> items, List<Rental> rentals) {
            this.members = members;
            this.items = items;
            this.rentals = rentals;
        }
    }
}