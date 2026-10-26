package com.example.pokedex.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pokemon")
public class PokemonEntity {
    @Id
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(name = "sprite_url")
    private String spriteUrl;

    private String category;
    private int weight;
    private int height;

    @Convert(converter = StringListConverter.class)
    @Column(nullable = false)
    private List<String> abilities = new ArrayList<>();

    @Column(name = "localized_name")
    private String localizedName;

    private String region;

    @Convert(converter = StringListConverter.class)
    @Column(nullable = false)
    private List<String> tags = new ArrayList<>();

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSpriteUrl() { return spriteUrl; }
    public void setSpriteUrl(String spriteUrl) { this.spriteUrl = spriteUrl; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    public List<String> getAbilities() { return abilities; }
    public void setAbilities(List<String> abilities) { this.abilities = new ArrayList<>(abilities); }
    public String getLocalizedName() { return localizedName; }
    public void setLocalizedName(String localizedName) { this.localizedName = localizedName; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = new ArrayList<>(tags); }
    public Long getVersion() { return version; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
